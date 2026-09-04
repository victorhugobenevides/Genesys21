# Plano de Implementação - Substituição de Instância Oracle Cloud

Este plano visa realizar o "Hard Reset" da infraestrutura na Oracle Cloud, substituindo a instância atual (que está com cache de imagem e problemas de acesso) por uma nova instância limpa e acessível.

## 🎯 Objetivos
- Terminar a instância antiga que não aceita atualizações.
- Criar uma nova instância com a chave SSH de resgate já injetada.
- Obter o novo IP público e preparar a Pipeline para o deploy final.

## 🛠️ Mudanças Propostas

### Infraestrutura (Oracle Cloud CLI)

#### 1. Terminar Instância Antiga
- Comando: `oci compute instance terminate --instance-id [OCID]`
- Isso liberará os recursos (CPU/RAM) da conta Always Free para a nova máquina.

#### 2. Criar Nova Instância
- **Shape**: `VM.Standard.E2.1.Micro` (Always Free)
- **Imagem**: Ubuntu (mesma da atual)
- **Rede**: Reutilizar a Subnet e VCN existentes.
- **SSH**: Injetar `~/.ssh/genesys_rescue_key.pub`.

#### 3. Configuração de DNS
- O IP público vai mudar. Após a criação, precisaremos atualizar o DNS de `victorbenevides.dev` no provedor (GoDaddy/Cloudflare/etc).

## 📅 Plano de Execução
1.  **Exclusão**: Deletar a instância atual.
2.  **Criação**: Subir a nova máquina via CLI.
3.  **Deploy**: Reconfigurar o CircleCI com o novo IP e rodar a Pipeline.
4.  **Verificação**: Validar o acesso SuperAdmin na nova infraestrutura.

> [!CAUTION]
> Ao prosseguir, o site ficará fora do ar até que o novo IP seja configurado no DNS e o deploy seja concluído.

## User Review Required

> [!IMPORTANT]
> Você confirma que tem acesso ao painel de DNS para atualizar o IP de `victorbenevides.dev` assim que a nova máquina subir?
