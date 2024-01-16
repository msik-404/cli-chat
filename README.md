# cli-chat
Simple java app which enables users to communicate. The app is basically echo server with many clients.

To build and run the app java 21 is required.

To build the app Run: 
```
./mvnw clean package
```

To run the server:
```
java -cp target/cli-chat-1.0-SNAPSHOT.jar com.msik404.clichat.CliChat server <PORT>
```

To run the client:
```
java -cp target/cli-chat-1.0-SNAPSHOT.jar com.msik404.clichat.CliChat client <HOST_NAME> <PORT> 
```
