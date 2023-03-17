# Build the frontend with vite
# Move frontend build to the static folder of the backend
# Build the backend with Maven
./build.sh
cd ../.. || exit

# Stop and remove old docker container if it exists
docker stop gleb-abramov-container || true
docker rm gleb-abramov-container || true

#Remove old docker image if it exists
docker rmi gleb-abramov || true

# Remove all dangling docker images (optionally)
# yes Y | docker image prune

# Build docker container image
docker build -t gleb-abramov .

# Run docker container image
docker run --detach --publish 3000:8080 --name gleb-abramov-container gleb-abramov