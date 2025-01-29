fiap-videoslice-svc-videohandler
================================


# Setup of local dev environment

Create postgres database

    sudo -u postgres psql  # Connect to PSQL as admin. The exact command may change depending on your distribution
      create user videoslice_user password 'Slice!';
      create database fiap_videoslice owner videoslice_user;

> Postgres may also be run with Docker. Use the appropriate env parameters to set the user, password and db name. 

Start LocalStack

    docker run --rm -it -d -p 4566:4566 -p 4510-4559:4510-4559 localstack/localstack

Examples of AWS Commands in LocalStack

    aws --endpoint-url=http://localhost:4566 --region us-east-1 sqs list-queues
    
    aws --endpoint-url=http://localhost:4566 --region us-east-1 sqs create-queue --queue-name videoslice_job_requests
    aws --endpoint-url=http://localhost:4566 --region us-east-1 sqs create-queue --queue-name videoslice_job_status

    aws --endpoint-url=http://localhost:4566 --region us-east-1 s3api list-buckets

    aws --endpoint-url=http://localhost:4566 --region us-east-1 s3api create-bucket --bucket videoslice-job-requests
    aws --endpoint-url=http://localhost:4566 --region us-east-1 s3api create-bucket --bucket videoslice-job-results

#### Run locally

    mvn spring-boot:run

#### Authorization

In local environment only a `Dummy` authorization schema is activated. Protected API call can be used with one of the
following HTTP Authorization headers:

    Authorization: Dummy User1
    Authorization: Dummy User2
    Authorization: Dummy Admin

They are mapped to mocked users that are valid through the application.

#### API Calls

See examples in the file test-resources/job-api.http
