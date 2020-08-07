cd ../gpconnect-provider-testing/scripts || exit
npm install
TEST_BASE_URL=http://localhost:8081/openmrs/ms/gpconnect/gpconnectServlet npm run setupData