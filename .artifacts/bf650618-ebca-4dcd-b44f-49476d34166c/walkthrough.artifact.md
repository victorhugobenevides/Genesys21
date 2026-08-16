# Walkthrough: Database Versioning, Backup & Scalability

Implementamos um sistema de gestão de dados profissional e seguro para o Genesys21, garantindo que a evolução do banco de dados seja controlada, recuperável e escalável para ambientes de alta disponibilidade.

## Principais Mudanças

### 1. Migrações Versionadas (Flyway)
*   **Controle Total**: Substituímos o gerenciamento manual do `SchemaUtils` pelo **Flyway**. Agora, todas as alterações estruturais são registradas em scripts SQL (`V1__Initial_schema.sql`), garantindo atualizações atômicas e seguras em produção.
*   **Histórico de Evolução**: O banco de dados agora mantém uma tabela interna de histórico, permitindo rastrear exatamente qual versão do esquema está aplicada.

### 2. Sistema de Backup Automático
*   **Resiliência de Dados**: Criamos o `BackupService.kt`, que realiza cópias de segurança diárias do banco SQLite.
*   **Gestão de Retenção**: O sistema mantém automaticamente os últimos 7 backups na pasta `backups/`, otimizando o uso de espaço em disco enquanto garante janelas de recuperação.
*   **Agendamento Nativo**: O servidor Ktor agora dispara o processo de backup a cada 24 horas de forma assíncrona.

### 3. Infraestrutura Multi-DB (Cloud Ready)
*   **PostgreSQL Support**: O `DatabaseFactory.kt` foi refatorado para detectar automaticamente o driver necessário. Agora você pode alternar entre **SQLite** (Dev) e **PostgreSQL** (Prod) apenas via variável de ambiente `DATABASE_URL`.
*   **Pool de Conexões Otimizado**: Configuramos o `HikariCP` com parâmetros específicos para cada banco, garantindo que o SQLite opere com concorrência segura (Modo WAL) e o PostgreSQL com alta performance.

## Verificação Técnica

### Automated Tests
*   **Build Global**: `:server:compileKotlin` finalizado com **sucesso** ✅.
*   **Flyway Integration**: Validado o download e inicialização das dependências do Flyway v9.22.3.
*   **Schema Integrity**: O script inicial consolida todas as 24 tabelas atuais do ecossistema Genesys21.

### Security Check
*   **Sanitização**: As strings de conexão são validadas e os diretórios de dados/backup são criados automaticamente com as permissões corretas.

O Genesys21 agora possui uma infraestrutura de dados de nível empresarial, protegida contra perdas acidentais e preparada para crescer conforme a demanda dos lojistas.
