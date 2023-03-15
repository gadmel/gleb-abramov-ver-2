cd frontend || exit
npm ci
npm run build
rm -rf ../backend/src/main/resources/static
mv build ../backend/src/main/resources/static
cd ../backend || exit
./mvnw clean package
cd ..
gcloud builds submit --tag gcr.io/gleb-abramov/app --project gleb-abramov
gcloud run deploy --image gcr.io/gleb-abramov/app --project gleb-abramov app --region europe-central2 --allow-unauthenticated --service-account github-cloudbuild@gleb-abramov.iam.gserviceaccount.com

