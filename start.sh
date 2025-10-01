echo "Starting Trivia Backend..."
cd Trivia_backend/demo
./mvnw clean package -DskipTests
java -jar target/demo-0.0.1-SNAPSHOT.jar & 

cd ../..

echo "Starting Trivia Frontend..."
cd trivia-frontend
npm install
ng serve --host 0.0.0.0 --port 4200