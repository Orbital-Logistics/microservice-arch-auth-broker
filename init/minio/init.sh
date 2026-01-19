mc alias set minio http://minio:9000 minio minio123

mc mb minio/user-files/reports
mc mb minio/default

mc cp /init/files/* minio/user-files/reports