# Resgate Final do Checkout e Deploy v6.1.5 🛡️💳

Este plano visa garantir que o servidor rode a versão **v6.1.5**, que contém as correções críticas para o Stripe (método de pagamento) e para o erro de CORS.

## User Review Required

> [!IMPORTANT]
> Como não temos **GitHub Actions**, o build das imagens Docker deve ser feito manualmente na sua máquina e enviado para o registro do GitHub (`ghcr.io`).

## Propostas de Mudança

### 1. Build e Empacotamento (Local)
Para que as correções de código surtam efeito no servidor `victorbenevides.dev`, precisamos gerar novos artefatos.

#### Passos Necessários:
1.  **Ligar o Docker Desktop**: Necessário para gerar as imagens.
2.  **Executar o Script de Build**: Rodar `./up.sh` no terminal do Android Studio.
3.  **Push das Imagens**: Enviar as novas versões para o GitHub.

### 2. Sincronização de Infraestrutura (Servidor)
Após o envio das imagens, entrarei no servidor via SSH para:
- Limpar o cache do Docker.
- Forçar o download da imagem **v6.1.5**.
- Validar se o link `/api/public/version` reflete a mudança.

## Verification Plan

### Verificação de Versão
- O link [https://victorbenevides.dev/api/public/version](https://victorbenevides.dev/api/public/version) deve retornar `v6.1.5`.

### Teste de Checkout
- Tentar realizar um agendamento. O modal do Stripe deve abrir com a opção de Cartão de Crédito ativa.

## Próximos Passos
Por favor, execute os comandos abaixo no seu terminal (Android Studio):

```bash
# 1. Build total do projeto
./up.sh

# 2. Login no GitHub Container Registry (se necessário)
# echo $GITHUB_TOKEN | docker login ghcr.io -u SEU_USUARIO --password-stdin

# 3. Enviar as imagens para o servidor buscar
docker push ghcr.io/victorhugobenevides/genesys-server:latest
docker push ghcr.io/victorhugobenevides/genesys-web:latest
```

**Assim que terminar o `docker push`, me avise para eu concluir o deploy no servidor!**
