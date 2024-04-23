docker pull postgres:latest
docker run --name mappin-produto-db -p 5433:5432 -e POSTGRES_USER=mappin -e POSTGRES_PASSWORD=mappinProduto -e POSTGRES_DB=mappin-produto-db -d postgres