## 🎓 ОЧЕНЬ ПОДРОБНОЕ ОБЪЯСНЕНИЕ ДОМАШКИ (ДЗ1)

### **НАЧАЛО: Что мы вообще делали раньше?**

На первой паре мы писали свои собственные **сервлеты** — это приложения, которые **связывают клиента и серверную часть**. Серверная часть — это наш компьютер, который выделяет ресурсы и выполняет операции. Формат работы: **сервер-клиент**, они общаются между собой через HTTP запросы. В нашем случае в качестве клиента служит **браузер пользователя**, а сервлетом было **наше Java приложение**, которое обрабатывает эти запросы и выводит HTML код прямо в браузер.

***

### **МИНИМАЛЬНАЯ ЛОГИКА ПРИЛОЖЕНИЯ**

Чтобы на странице вывести "Hello!", нам нужно:

#### **1️⃣ Открыть серверный сокет на порту 8080** (public class HttpServer)
```java 
ServerSocket serverSocket = new ServerSocket(8080); ()
```
**Что это?**
- **Сокет** — это механизм обмена данными между сервером и клиентом (как коммуникационный канал)
- **Порт 8080** — это просто номер, по которому будет происходить общение. Можно выбрать любой, но 8080 — стандартный для локальных приложений

#### **2️⃣ Запустить бесконечный цикл**
```java
while (true) {
    Socket clientSocket = serverSocket.accept();
    new Thread(() -> requestHandler.handle(clientSocket)).start();
}
```
**Зачем?** Если не будет бесконечного цикла, то после одной отправки сообщения клиенту наше приложение просто закроется. А нам нужно, чтобы оно работало все время, и клиент мог в любой момент обратиться к серверу и получить ответ.

#### **3️⃣ serverSocket.accept() — ждем клиента**
```java
Socket clientSocket = serverSocket.accept();
```
**Что происходит?**
- Этот метод **ждет подключения клиента** (то есть ждет, когда кто-то откроет браузер и введет `localhost:8080`)
- Когда это происходит, метод возвращает **клиентский сокет** — это сокет для передачи данных конкретному клиенту

#### **4️⃣ Вытаскиваем outputStream из сокета**public class RequestHandler(request handler (while))
```java
OutputStream os = clientSocket.getOutputStream();
```
**В чем отличие от сокета?**
- **Сокет** — это более обширное понятие, содержит много информации (и данные входящие, и выходящие)
- **OutputStream** — это просто **поток данных, в который мы записываем** информацию для отправки клиенту

#### **5️⃣ Формируем HTTP ответ по стандарту**
```java
os.write("HTTP/1.1 200 OK\r\n".getBytes());
os.write("Content-Type: text/html;charset=UTF-8\r\n".getBytes());
os.write("\r\n".getBytes());
```
**Что это?**
- **HTTP/1.1** — версия протокола
- **200** — код статуса (успешно)
- **OK** — текстовое описание статуса
- **Content-Type** — служебная информация, что мы отправляем HTML

#### **6️⃣ Передаем сам HTML код**
```java
os.write("<html><body>Hello!</body></html>".getBytes());
```
Вот это и выведется в браузер.

#### **7️⃣ getBytes() — преобразуем строку в байты**
```java
os.write("HTTP/1.1 200 OK\r\n".getBytes());
```
**Почему это нужно?**
- **OutputStream** принимает только **байты**, он не понимает обычные строки, числа и прочее
- `getBytes()` преобразует строку в массив байтов

#### **8️⃣ В конце принудительно отправляем данные**
```java
os.flush();
clientSocket.close();
```
- **flush()** — гарантирует, что все данные отправлены клиенту
- **close()** — закрываем соединение

***

### **НО ПОТОМ МЫ ПОДУМАЛИ... 🤔**

Мы понимали, что **писать все в одном классе неудобно**. Если захотим выводить несколько разных страничек, придется писать огромный if-else или switch, и весь код будет в одном месте.

**Решение:** **Делегировать задачу другим классам**!

Создали класс **RequestHandler**, который:
- **Ловит запрос** от клиента
- **Определяет**, какой класс вызвать в зависимости от ресурса (пути)
- **Передает работу** этому классу

***

### **КЛАСС RequestHandler — СЕРДЦЕ ПРИЛОЖЕНИЯ**

```java
public void handle(Socket clientSocket) {
    BufferedReader reader = new BufferedReader(
        new InputStreamReader(clientSocket.getInputStream()));
    
    String lineOne = reader.readLine();
    System.out.println(lineOne); // Выводим, что пришло
```

**Первый шаг:** Мы берем **InputStream** из сокета (это входящие данные от клиента) и оборачиваем его в:
- **InputStreamReader** — преобразует байты в символы/строки
- **BufferedReader** — читает более эффективно и работает сразу со строками

Потом читаем **первую строку** с помощью `readLine()` — это главная информация о запросе.

***

### **ЧТО ПРИХОДИТ ОТ КЛИЕНТА?**

Когда мы заходим в браузер и вводим `http://localhost:8080/hello`, сервер получает:

```
GET /hello HTTP/1.1
Host: localhost:8080
Connection: keep-alive
... (остальные заголовки)
```

**Первая строка:** `GET /hello HTTP/1.1`

Давай разберем каждое слово:
- **GET** — тип HTTP запроса (метод)
    - **GET** — просто запрашиваем информацию с сервера, ничего не передаем
    - **POST** — запрашиваем информацию, но передаем дополнительные данные
- **/hello** — ресурс (путь, который идет после порта)
- **HTTP/1.1** — версия протокола

***

### **ПАРСИНГ ЗАПРОСА В RequestHandler**

```java
String[] components = lineOne.split(" ");
String method = components[0];        // GET
String resource = components [ppl-ai-file-upload.s3.amazonaws](https://ppl-ai-file-upload.s3.amazonaws.com/web/direct-files/attachments/74633895/68687d73-ab61-4af4-965b-59663cd894a3/domashki.docx);      // /hello?name=Sasha&age=18
String path = resource;
```

Разбиваем первую строку по пробелам и вытаскиваем **метод** и **ресурс**.

**Но внимание!** Если есть параметры, ресурс выглядит как `/hello?name=Sasha&age=18`, и нам нужно из него отделить:
- **path** — `/hello`
- **параметры** — `name=Sasha&age=18`

```java
int queryIndex = resource.indexOf('?');
if (queryIndex != -1) {
    path = resource.substring(0, queryIndex);  // /hello
    String query = resource.substring(queryIndex + 1); // name=Sasha&age=18
    
    String[] queryParams = query.split("&"); // [name=Sasha, age=18]
    
    for (String queryParam : queryParams) {
        String[] keyValue = queryParam.split("=", 2);
        String key = keyValue[0];     // name
        String value = keyValue [ppl-ai-file-upload.s3.amazonaws](https://ppl-ai-file-upload.s3.amazonaws.com/web/direct-files/attachments/74633895/68687d73-ab61-4af4-965b-59663cd894a3/domashki.docx);   // Sasha
        params.put(key, value);       // Кладем в Map
    }
}
```

**Результат:** Map с параметрами
```
params: {name=Sasha, age=18}
Было:  "/hello?name=Sasha&age=18"
       
Стало:
       path = "/hello"
       params = Map {
           "name" → "Sasha",
           "age" → "18"
       }

```

***

### **ДАЛЬШЕ В RequestHandler — ЗНАКОМАЯ ЛОГИКА**

```java
while (true) {
    String message = reader.readLine();
    
    if (message.isEmpty()) {
        logger.debug("end of request header");
        OutputStream os = clientSocket.getOutputStream();
        
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
```

Мы читаем все остальные строки (заголовки) из запроса в цикле, пока не встретим пустую строку (это означает конец заголовков HTTP запроса).

Когда встретили пустую строку:
1. Вытаскиваем **OutputStream** (куда писать ответ)
2. Ищем в **Application.resourceMap** нужный обработчик по пути (`/hello`)
3. Если нашли — вызываем его метод `service(method, params, os)`
4. Если не нашли — вызываем `NotFoundService` (404 ошибка)
5. Отправляем данные и закрываем соединение

***

### **КАК ЭТО РАБОТАЕТ ВМЕСТЕ?**

**Общая логика:**

1. **HttpServer** запускается, вызывает `Application.init()` — инициализирует Map обработчиков
2. **HttpServer** входит в бесконечный цикл и ждет клиентов
3. Когда клиент подключается (берет браузер и заходит на `localhost:8080`), вызывается **RequestHandler.handle()**
4. **RequestHandler** смотрит, что пришло от клиента (парсит первую строку)
5. Вытаскивает **path**, **method** и **params** из запроса
6. Смотрит в **Application.resourceMap** — есть ли обработчик для этого пути
7. Если есть (например, `/home` есть в map) — вызывает **HomeService**
8. **HomeService** пишет HTTP ответ в outputStream
9. Ответ отправляется клиенту в браузер

**Результат:** Браузер выводит "Hello!"

***

### **ПОЧЕМУ ЭТО ЛУЧШЕ?**

**До:**
```java
// Все в HttpServer, огромный класс
if (path.equals("/home")) { ... HTML для home ... }
else if (path.equals("/about")) { ... HTML для about ... }
else if (path.equals("/contact")) { ... HTML для contact ... }
// Кошмар!
```

**Теперь:**
```java
// Каждая страница — свой класс
class HomeService implements IResourceService { ... }
class AboutService implements IResourceService { ... }
class ContactService implements IResourceService { ... }

// Просто добавляем в Map
resourceMap.put("/home", new HomeService());
resourceMap.put("/about", new AboutService());
// Чистый код!
```

Это паттерн **Strategy** или **Command** — очень мощный инструмент! 🚀