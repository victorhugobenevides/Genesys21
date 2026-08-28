# Plano de Implementação - Promoção Garantida de SuperAdmin

O usuário `victorkoto@gmail.com` está enfrentando problemas onde, ao logar via Google, ele não é imediatamente reconhecido como `SUPERADMIN` no front-end, resultando em uma tela administrativa sem as opções privilegiadas.

## Análise

1.  **Estado do Front-end (Bug)**: Na função `syncInitialProfile` do `PageViewModel`, quando um novo perfil é criado localmente para sincronização, ele é inicializado com `role = UserRole.CUSTOMER`. Após salvar no servidor (onde o servidor força a promoção para `SUPERADMIN` baseada no e-mail), o front-end atualiza o estado local com o objeto que tem o cargo de `CUSTOMER`, em vez de recarregar o perfil atualizado do servidor.
2.  **Persistência**: Embora o banco de dados esteja correto, o estado reativo do Compose fica preso no valor inicial até que o App seja reiniciado ou o perfil seja recarregado manualmente.
3.  **Visibilidade de Abas**: No `PageListScreen`, a aba "SuperAdmin" depende estritamente do `userProfile?.role == UserRole.SUPERADMIN`.

## Mudanças Propostas

### [composeApp](file:///Users/victorben/AndroidStudioProjects/genesys21/composeApp)

#### [MODIFY] [PageViewModel.kt](file:///Users/victorben/AndroidStudioProjects/genesys21/composeApp/src/commonMain/kotlin/com/itbenevides/genesys21/presentation/PageViewModel.kt)
- Atualizar `syncInitialProfile` para invocar `loadUserProfile(userId)` imediatamente após o sucesso do `saveUserProfileUseCase`. Isso garante que o front-end receba o cargo de `SUPERADMIN` (e as permissões completas) que o servidor atribuiu.
- Remover a atribuição manual `_userProfile.value = newProfile` dentro do bloco de sucesso, pois o `loadUserProfile` fará isso com os dados reais do banco.

## Plano de Verificação

### Verificação Manual
- Solicitar ao usuário que faça Logout e Login novamente após a aplicação da correção.
- Verificar se a aba "SuperAdmin" aparece no menu lateral/inferior.
- Confirmar se as opções de gerenciamento de usuários e sistema estão visíveis.

### Logs
- Observar os logs do console: deve aparecer "VIEWMODEL: Perfil inicial sincronizado com sucesso" seguido pelo carregamento do perfil real.
