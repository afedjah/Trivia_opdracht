echo "Starting Trivia Backend..."
cd Trivia_backend/demo
./mvnw clean package -DskipTests
#mvn clean package -DskipTests
java -jar target/demo-0.0.1-SNAPSHOT.jar & 

cd ../..
sleep 5

echo "Starting Trivia Frontend..."
cd trivia-frontend
npm install
npm run build
#ng serve --host 0.0.0.0 --port 4200
exec npx serve -s dist/trivia-frontend -l $PORT