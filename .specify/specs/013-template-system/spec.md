# Spec 013: Page Template System & Catalog

## 1. Overview
The **Page Template System** allows merchants to quickly launch high-quality vitrines by choosing from a curated catalog of pre-configured layouts. This reduces friction for new users and ensures that even non-designers can achieve a professional aesthetic.

## 2. Core Concepts
- **Template Definition**: A predefined list of `PageComponent` objects, a `PageThemeConfig`, and default metadata.
- **Catalog**: A visual gallery where users can preview and select a template.
- **Factory Pattern**: Centralized logic to instantiate templates with unique IDs and linked `storeId`.

## 3. Existing Templates (Standardization)
We currently have several hardcoded templates in `Page.kt`. These should be standardized:
- **Professional Vitrine**: Focused on products and quick sales.
- **Link in Bio**: Minimalist profile header + social buttons.
- **Barber Shop**: Booking-centric layout with service lists and calendar.
- **Pro Design**: High-impact layout with glassmorphic cards and large imagery.
- **Blog Post**: Content-focused layout with text blocks and category filters.

## 4. Feature Requirements

### 4.1. Template Metadata
Every template must include:
- `title`: Display name in the catalog.
- `description`: Short explanation of the ideal use case.
- `thumbnailUrl`: A snapshot of the template in action (or a representative icon).
- `category`: e.g., "Sales", "Services", "Personal".

### 4.2. Template Catalog Screen
- A new screen/dialog (accessible via "New Page") that displays cards for each template.
- **Live Preview**: Ability to see a static rendering of the template before committing.
- **Empty State**: Option to start from a blank canvas.

### 4.3. Persistence & Serialization
- Templates should ideally be defined in a `templates.json` file or a specialized registry in `shared` to allow for server-side updates without app redeployment.

## 5. Technical Implementation

### 5.1. Domain Layer (Shared)
- `PageTemplate` model.
- `GetTemplatesUseCase`: Returns the list of available blueprints.
- Refactor `Page.Companion` factory methods into a dedicated `PageTemplateRegistry`.

### 5.2. Presentation Layer (ComposeApp)
- `TemplateCatalogScreen`: Responsive grid of templates.
- Integration into the `CreatePageDialog`.

## 6. Success Criteria
- [ ] Merchant can create a "Barber Shop" page with one click.
- [ ] Catalog UI adapts correctly to Mobile and Desktop.
- [ ] Adding a new template requires zero changes to the `WhiteLabelScreen` rendering logic.
- [ ] Templates include at least 5 default components each.
