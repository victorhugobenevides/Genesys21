# Especificação: Ambiente de Pré-Produção na Oracle Cloud (OCI)

Este documento detalha a infraestrutura e o processo de implantação para criar um ambiente de pré-produção robusto e escalável para o ecossistema Genesys21 na Oracle Cloud Infrastructure (OCI).

## Objetivos
*   Espelhar o ambiente de produção o mais fielmente possível.
*   Garantir persistência de dados (SQLite e Uploads).
*   Configurar SSL automático com Certbot.
*   Utilizar Docker para isolamento e facilidade de deploy.

## Arquitetura Proposta

### 1. Computação (Compute)
*   **Instância VM:** `VM.Standard.E4.Flex` (ou `VM.Standard.A1.Flex` se ARM for preferível pela eficiência de custo).
*   **Sistema Operacional:** Oracle Linux 8 ou 9 (otimizado para OCI).
*   **Engine:** Docker + Docker Compose instalado.

### 2. Rede (Networking - VCN)
*   **VCN:** `vcn-genesys21-preprod`
*   **Sub-rede:** Pública com Internet Gateway.
*   **Security List (Ingress):**
    *   `TCP 22` (SSH - restrito a IPs específicos se possível).
    *   `TCP 80` (HTTP - para redirecionamento e validação Certbot).
    *   `TCP 443` (HTTPS - tráfego seguro do usuário).
    *   `TCP 8080` (Opcional: Acesso direto à API para debug, se necessário).

### 3. Armazenamento (Storage)
*   **OCI Block Volume:** 50GB+ montado em `/mnt/genesys_data`.
*   **Mapeamento Docker:**
    *   `/mnt/genesys_data/sqlite:/app/data`
    *   `/mnt/genesys_data/uploads:/app/uploads`
*   **Backups:** Política de backup automático (Silver/Gold) configurada no OCI.

### 4. Segurança e Identidade
*   **IAM:** Compartimento dedicado `genesys21-preprod`.
*   **Secret Management:** Uso do **OCI Vault** para armazenar `STRIPE_SECRET_KEY` e outras chaves sensíveis, injetando-as como variáveis de ambiente no deploy.

---

## Processo de Implantação (Deploy)

### Passo 1: Preparação da Imagem
O CI/CD (ex: CircleCI ou GitHub Actions) deve gerar as imagens Docker e enviá-las para o **OCI Container Registry (OCIR)**.

### Passo 2: Configuração da Instância
Script de provisionamento (User Data) para:
1.  Instalar Docker e Docker Compose.
2.  Formatar e montar o Block Volume.
3.  Autenticar no OCIR.

### Passo 3: Orquestração (docker-compose.yml)
Ajustar o `docker-compose.yml` para pré-produção:
```yaml
services:
  server:
    image: <region>.ocir.io/<tenancy>/genesys21/server:preprod
    environment:
      - STRIPE_SECRET_KEY=${PREPROD_STRIPE_SECRET}
      - PUBLIC_HOST=preprod.genesys21.com
    volumes:
      - /mnt/genesys_data/sqlite:/app/data
      - /mnt/genesys_data/uploads:/app/uploads

  web:
    image: <region>.ocir.io/<tenancy>/genesys21/web:preprod
    ports:
      - "80:80"
      - "443:443"
    volumes:
      - /mnt/genesys_data/certbot/conf:/etc/letsencrypt
```

---

## User Review Required

> [!IMPORTANT]
> **Domínio:** Precisamos de um subdomínio (ex: `preprod.itbenevides.com.br`) apontando para o IP Público da instância na Oracle.

> [!CAUTION]
> **Persistência SQLite:** Como estamos usando SQLite, não podemos escalar horizontalmente o serviço `server` sem migrar para um banco externo (ex: OCI MySQL ou PostgreSQL). Para pré-produção, o Block Volume resolve a persistência em uma única instância.

## Próximos Passos
1.  Aprovação desta especificação.
2.  Criação do script Terraform (opcional) ou manual via OCI Console.
3.  Configuração das Variáveis de Ambiente no Painel OCI.
4.  Execução do primeiro deploy.
