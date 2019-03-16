To generate mongo image type below command

docker build -t ecartify/mongo

After that to start type

docker run -d -p 27017:27017 -env MONGODB_PASSWORD=<password> ecartify/mongo

you can get the password from the config server by calling:

http://localhost:8888/config-service/auth-service/dev

After starting the config server it will ask for config server password.

