# Plano de Implementação - Choque de Deploy v5.3

Este plano visa forçar a atualização do servidor Oracle Cloud, tratando o erro 404 (versão antiga persistente) e garantindo que o novo código de segurança entre no ar.

## 🔍 Diagnóstico
- O servidor remoto está ignorando os comandos de `docker pull`.
- A rota `/api/public/version` retorna 404 porque o container antigo não possui essa definição.

## 🛠️ Mudanças Propostas

### [server](file:///Users/victorben/AndroidStudioProjects/genesys21/server)

#### [MODIFY] [Application.kt](file:///Users/victorben/AndroidStudioProjects/genesys21/server/src/main/kotlin/com/itbenevides/genesys21/Application.kt)
- Mover a rota de versão para o topo do `routing` para evitar interferência de outros blocos.
- Adicionar log de "DEPLOY_SUCCESS" no console de boot.

#### [MODIFY] [.circleci/config.yml](file:///Users/victorben/AndroidStudioProjects/genesys21/.circleci/config.yml)
- Alterar o comando de deploy para remover imagens órfãs e forçar a recriação do container de forma atômica.
- Aumentar o timeout de conexão SSH para evitar falhas em rede instável.

## 📅 Plano de Verificação
1.  Aguardar conclusão do job `deploy` no CircleCI.
2.  Acessar `https://victorbenevides.dev/api/public/version`.
3.  **Critério de Aceite**: A página deve exibir "Genesys21 Stable v5.1 - Final Strike".
4.  Após a confirmação da versão, o SuperAdmin e as Vitrines estarão ativos.
