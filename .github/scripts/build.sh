# Build the frontend with vite
cd ../../frontend || exit
npm ci || exit
npm run build

# Move frontend build to the static folder of the backend (cleaning it first)
rm -rf ../backend/src/main/resources/static
mv dist ../backend/src/main/resources/static

# Build the backend with Maven
cd ../backend || exit
./mvnw clean package
