Для создания запускного файла сервера перейти в каталог TanksServer и выполнить команду:
    mvn package
Для создания запускного файла клиента перейти в каталог TanksClient и выполнить команду:
    mvn package
Необходимо предварительно создать базу данных в Postgres. Команда для создания прописана в файле src/ex00/TanksServer/src/main/resources/schema.sql, при необходимости можно отредактировать файл src/ex00/TanksServer/src/main/resources/db.properties для доступа к базе данных.
Запустить сервер можно из каталога TanksServer нажатием мыши по файлу socket-server.jar либо из терминала командой
    java -jar socket-server.jar
Запустить клиент можно из каталога TanksClient нажатием мыши по файлу socket-client.jar либо из терминала командой
    java -jar socket-client.jar