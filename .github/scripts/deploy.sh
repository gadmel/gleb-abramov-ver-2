# Build the frontend with vite
# Move frontend build to the static folder of the backend
# Build the backend with Maven
./build.sh

# Build a Docker image on Google Cloud Build
# Save it to Google Container Registry
# Deploy the container image to Google Cloud Run
# Rewrite the Firebase hosting configuration to point to the new Cloud Run URL


cd ../.. || exit
gcloud builds submit --config cloudbuild.yml

# see the full instructions on how to set up Firebase hosting with Cloud Run
# https://firebase.google.com/docs/hosting/cloud-run
