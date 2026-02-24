package ru.itis.dis403.lab1_01;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class RequestHandler {
    final static Logger logger = LogManager.getLogger(RequestHandler.class);

    public void handle(Socket clientSocket) {
        try {
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(clientSocket.getInputStream()));

            String lineOne = reader.readLine();
            System.out.println(lineOne);
            logger.debug(lineOne);

            String[] components = lineOne.split(" ");
            String method = components[0];
            String resource = components[1];
            // /hello?name=Elvina&age=19
            String path = resource;
            Map<String, String> params = new HashMap<>();

            // http://localhost:8080/resource/part?/hello?name=Elvina&age=19
            // URI /resource/part
            // /hello?name=Elvina&age=19
            int queryIndex = resource.indexOf('?');
            if (queryIndex != -1) {
                path = resource.substring(0, queryIndex); // /hello
                String query = resource.substring(queryIndex + 1); // name=Elvina&age=19
                String[] queryParams = query.split("&"); // [name=Elvina, age=19]

                for (String queryParam : queryParams) {
                    String[] keyValue = queryParam.split("=", 2); // 2 = max деление на 2 части только
                    // name=Elvina
                    String key = keyValue[0]; // name
                    String value = keyValue.length > 1 ? keyValue[1] : ""; // Elvina
                    params.put(key, value);
                }
            }

            if (path.equals("/shutdown")) {
                logger.info("server stopped by client");
                clientSocket.close();
                return;
            }

            while (true) {
                String message = reader.readLine();
                System.out.println(message);
                logger.debug(message);

                if (message.isEmpty()) {
                    logger.debug("end of request header");
                    OutputStream os = clientSocket.getOutputStream();
                    logger.debug("outputStream" + os);

                    IResourceService resourceService = Application.resourceMap.get(path);
                    if (resourceService != null) {
                        resourceService.service(method, params, os);
                    } else {
                        new NotFoundService().service(method, params, os);
                    }

                    os.flush();
                    clientSocket.close();
                    break;
                }
            }
        } catch (IOException e) {
            logger.error("Error handling client request", e);
        }
    }
}
