#!/bin/bash

export AWS_ACCESS_KEY_ID=000000000000 AWS_SECRET_ACCESS_KEY=000000000000

# Create SQS queues
awslocal sqs create-queue --queue-name videoslice_job_requests
awslocal sqs create-queue --queue-name videoslice_job_status

# Create S3 buckets
awslocal s3api create-bucket --bucket videoslice-job-requests
awslocal s3api create-bucket --bucket videoslice-job-results
