# Project Specification: WhiteLabel Editor & Viewer

## Overview
This specification details the requirements for the `WhiteLabelScreen` and its related components within the Genesys21 platform. The goal is to provide a robust, high-performance editor for vitrine creation and a reliable viewer for end-customers, ensuring consistent behavior across Android, iOS, and WasmJs targets.

## Functional Requirements
1. **Polymorphic Component Rendering**: The system must correctly serialize and deserialize all `PageComponent` subclasses (Header, Text, Image, ProductList, ProfileHeader, etc.) using Kotlinx Serialization.
2. **Dynamic Editor (WhiteLabelScreen)**:
   - Users must be able to add, remove, and reorder components.
   - Real-time preview of the page must be available during editing.
   - Changes must be saved as a draft locally and can be published to the server.
3. **Component-Specific Editors**:
   - **ProfileHeader**: Support image upload and bio editing.
   - **ProductList**: Allow dynamic selection of products and horizontal/vertical layout toggling.
   - **Image**: Support both external URLs and local uploads.
4. **Offline Draft Support**: The `PageViewModel` must use `PageDraftRepository` to persist unsaved changes across app restarts.
5. **Responsive Layouts**: UI must adapt between mobile (single column) and desktop (split editor/preview) views using `BoxWithConstraints`.
6. **Gitflow Workflow Implementation**:
   - `main`: Branch para código estável em produção.
   - `develop`: Branch de integração para novas funcionalidades e bugfixes.
   - `feature/*` e `bugfix/*`: Branches de trabalho curtas, criadas a partir da `develop`.
   - `release/*`: Branches temporárias para preparação de versão e tags.
   - `hotfix/*`: Branches críticas criadas a partir da `main` para correções urgentes.

## Non-Functional Requirements
1. **Compilation Stability**: No target-specific code should prevent compilation of shared modules (WasmJs/Common).
2. **Performance**: List rendering in `WhiteLabelContent` must be smooth, utilizing lazy loading and keys for item stability.
3. **Type Safety**: Use of `expect/actual` must be consistent across all supported platforms (Android, iOS, WasmJs).
4. **Security & Protection**:
   - Enforce branch protection: No direct pushes to `main` or `develop`.
   - All merges must occur via Pull Requests with at least 1 mandatory review.
   - Mandatory CI checks for every PR (lint, build, unit tests).

## User Stories
### 1. Boutique Owner Customization
- **As a** boutique owner,
- **I want to** easily add a "Profile Header" with my logo and bio,
- **So that** my customers recognize my brand immediately.
- **Acceptance Criteria**:
  - Image picker opens on click.
  - Image preview updates immediately in the editor.
  - Bio text updates are persisted in the local draft.

### 2. Customer Product Browsing
- **As a** customer,
- **I want to** see a stable list of products that doesn't "jump" when images load,
- **So that** I have a smooth shopping experience.
- **Acceptance Criteria**:
  - LazyColumn items use stable keys.
  - Images have placeholders during loading.

## Success Metrics
- **Zero Compilation Errors**: Successful build for all targets (`:composeApp:assembleDebug`, `:composeApp:compileKotlinWasmJs`).
- **State Integrity**: 100% of defined `PageComponent` types are successfully rendered in both Edit and View modes.
