# NotificationService

Сервер jetty запускается из папки с pom с помощью команды mvn jetty:run.
Для отправки email необходимо инициализировать константы класса Main (SMTP_HOST, SMTP_PORT, USER_NAME, PASSWORD).
Пример http запроса: 'http://localhost:8080/?externalId=12&message=qwerty&time=2016.09.18at16:43:30&extraParams=lizap@bk.ru&NotificationType=mail'
                    'http://localhost:8080/?externalId=12&message=qwerty&time=2016.09.18at16:43:30&extraParams=localhost:8888&NotificationType=http'
