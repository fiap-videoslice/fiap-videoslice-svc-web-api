# fiap-videoslice-svc-videohandler



# Setup of local dev environment

Create postgres database

    sudo -u postgres psql  # Connect to PSQL as admin. The exact command may change depending on your distribution
      create user videoslice_user password 'Slice!';
      create database fiap_videoslice owner videoslice_user;

Start LocalStack

    docker run --rm -it -d -p 4566:4566 -p 4510-4559:4510-4559 localstack/localstack

Examples of AWS Commands in LocalStack

    aws --endpoint-url=http://localhost:4566 --region us-east-1 sqs list-queues
    
    aws --endpoint-url=http://localhost:4566 --region us-east-1 sqs create-queue --queue-name videoslice_job_requests
    aws --endpoint-url=http://localhost:4566 --region us-east-1 sqs create-queue --queue-name videoslice_job_status

    aws --endpoint-url=http://localhost:4566 --region us-east-1 s3api list-buckets

    aws --endpoint-url=http://localhost:4566 --region us-east-1 s3api create-bucket --bucket videoslice-job-requests
    aws --endpoint-url=http://localhost:4566 --region us-east-1 s3api create-bucket --bucket videoslice-job-results

