# Tasks: Page Template System

- [x] T001 Define `PageTemplate` model in `shared/domain/model/Page.kt`
- [x] T002 Migrate hardcoded `Page.profileTemplate`, `Page.barberShopTemplate` etc., to a new `PageTemplateRegistry`
- [x] T003 Implement `GetTemplatesUseCase` to fetch all available blueprints
- [x] T004 Create `TemplateCatalogScreen` in `:composeApp` with a responsive grid
- [x] T005 Update `CreatePageDialog` in `PageListScreen.kt` to use the new visual catalog
- [x] T006 Add "Preview" functionality to the catalog cards
- [x] T007 Verify WasmJs performance when rendering the catalog grid
