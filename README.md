# This should be....

## Install LocalStack and awscli
On Mac:
```shell
brew install localstack
brew install awscli
```

## Testing locally using LocalStack
### Starting LocalStack
```shell
localstack start
```
## Creating an S3 bucket and copying an image to it
```shell
export LOCALSTACK_ENDPOINT=http://localhost:4566
aws --endpoint-url=$LOCALSTACK_ENDPOINT s3 mb s3://getawaydle-source-bucket
```

### Verifying the contents of the bucket
There may be an easier way to do this, or at least away to do so from outside of the
container, but this at least works.

```shell
docker ps -a | grep -i localstack
docker exec -it localstack-main bash

awslocal s3api list-buckets
awslocal s3api list-objects --bucket getawaydle-source-bucket

```









## references
https://docs.aws.amazon.com/lambda/latest/dg/with-s3-example.html
Baeldung [A Basic AWS Lambda Example](https://www.baeldung.com/java-aws-lambda)
[Baeldung Source Code](https://github.com/eugenp/tutorials/tree/master/aws-modules/aws-lambda-modules/lambda-function)
[LocalStack Docs](https://docs.localstack.cloud/getting-started/installation/)
[LocalStack S3 Docs](https://docs.localstack.cloud/user-guide/aws/s3/)
[AWS Serverless Snippets](https://github.com/aws-samples/serverless-snippets)

