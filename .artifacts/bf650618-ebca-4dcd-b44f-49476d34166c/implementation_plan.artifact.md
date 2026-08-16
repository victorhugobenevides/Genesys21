# Plan: Database Versioning, Backup & Scalability

Este plano visa profissionalizar a gestão de dados do Genesys21, migrando de uma criação manual de tabelas para um sistema robusto de migrações versionadas (Flyway), implementando backups automáticos e preparando a infraestrutura para escala (suporte a PostgreSQL).

## User Review Required

> [!IMPORTANT]
> A migração para o **Flyway** substituirá o `DatabaseMigrator.kt`. A partir de agora, qualquer alteração no banco de dados deve ser feita através de um novo arquivo `.sql` na pasta de migrações, garantindo que nenhum dado seja perdido em atualizações de produção.

> [!WARNING]
> A funcionalidade de **Backup** inicial focará em SQLite (cópia de arquivo). Para PostgreSQL em produção, recomenda-se o uso de ferramentas nativas (pg_dump) ou backups gerenciados pelo provedor de nuvem.

## Proposed Changes

### [Server - Infrastructure]
#### [MODIFY] [libs.versions.toml](file:///Users/victorben/AndroidStudioProjects/genesys21/gradle/libs.versions.toml)
- Adicionar dependências do Flyway: `flyway-core`, `flyway-database-sqlite` e `flyway-database-postgresql`.

#### [MODIFY] [server/build.gradle.kts](file:///Users/victorben/AndroidStudioProjects/genesys21/server/build.gradle.kts)
- Incluir as novas dependências no módulo servidor.

#### [MODIFY] [DatabaseFactory.kt](file:///Users/victorben/AndroidStudioProjects/genesys21/server/src/main/kotlin/com/itbenevides/genesys21/data/database/DatabaseFactory.kt)
- Integrar o Flyway no ciclo de vida de inicialização.
- Implementar detecção automática de driver (SQLite vs PostgreSQL) baseada na URL de conexão.
- Adicionar suporte a `clean` via flag apenas em ambiente de desenvolvimento.

#### [NEW] [V1__Initial_schema.sql](file:///Users/victorben/AndroidStudioProjects/genesys21/server/src/main/resources/db/migration/V1__Initial_schema.sql)
- Criar o script SQL inicial consolidando todas as tabelas atuais com as restrições e índices corretos.

### [Server - Backup System]
#### [NEW] [BackupService.kt](file:///Users/victorben/AndroidStudioProjects/genesys21/server/src/main/kotlin/com/itbenevides/genesys21/data/service/BackupService.kt)
- Implementar utilitário para realizar cópias de segurança do banco SQLite.
- Nomeação rotativa: `genesys21_backup_YYYYMMDD.db`.

#### [MODIFY] [Application.kt](file:///Users/victorben/AndroidStudioProjects/genesys21/server/src/main/kotlin/com/itbenevides/genesys21/Application.kt)
- Configurar uma tarefa agendada (coroutine) para rodar o backup diariamente.

## Verification Plan

### Automated Tests
- Executar o servidor com um banco limpo e verificar se o Flyway cria todas as tabelas.
- Adicionar uma migração `V2__Test.sql` e verificar se ela é aplicada sem erros.

### Manual Verification
- Forçar um backup via endpoint (ou no startup) e verificar se o arquivo `.db` gerado na pasta `backups/` é válido e contém os dados atuais.
- Tentar rodar o servidor apontando para um PostgreSQL local para validar a escalabilidade da configuração.
