# Walkthrough - Estabilização e Reforço de Segurança (Hardening)

Corrigi as falhas nos testes de segurança no ambiente de CI e reforcei a proteção contra ataques de *Mass Assignment* e *Price Manipulation*.

## 🛡️ Melhorias e Correções

### 1. Reforço no Cadastro de Usuários (Mass Assignment)
- **O que foi feito**: Modifiquei o repositório `SqliteUserRepository.kt` para forçar o cargo `CUSTOMER` em todas as novas inserções de usuários.
- **Por que é importante?**: Impede que um atacante, ao criar uma conta via API, envie um campo `role: "SUPERADMIN"` no JSON e ganhe acesso total ao sistema. A única exceção é o e-mail de administrador oficial (dogma).

### 2. Estabilização do Teste de Recálculo de Preços
- **O que foi feito**: Corrigi o setup do teste `SecurityHardeningTest.kt` para garantir que a loja (`Store`) exista no banco de dados antes da criação do pedido.
- **Resultado**: Resolvido o erro "Pedido não encontrado", permitindo validar que o servidor ignora o preço forjado pelo front-end e recalcula o valor real com base no banco de dados.

### 3. Isolamento de Banco de Dados em Testes
- **O que foi feito**: Implementei o uso de nomes de bancos de dados únicos (`System.nanoTime()`) para cada execução de teste unitário.
- **Por que é importante?**: Evita que testes rodando em paralelo no CI interfiram uns nos outros através do cache compartilhado do SQLite em memória, garantindo resultados determinísticos.

## 📄 Conclusão
Com estas mudanças, a infraestrutura de segurança do Genesys21 está mais resiliente e os testes automatizados agora refletem cenários reais de ataque, garantindo que as proteções de lógica de negócio permaneçam ativas.

> [!IMPORTANT]
> As correções foram aplicadas e o código já foi enviado para o branch `main`. A pipeline deve agora apresentar o status "Verde".
