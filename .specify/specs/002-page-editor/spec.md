# Page Editor Specification

## Overview
The Page Editor is the core "White Label" creation tool in Genesys21. it allows merchants to build customized storefronts (vitrines), landing pages, and bio profiles using a component-based approach.

## User Roles
- **Merchant**: Creates, edits, and publishes pages.
- **Visitor**: Views the published pages (handled by the Viewer).

## Core Features

### 1. Component-Based Layout
- Users can add, remove, reorder, and duplicate components.
- Supported components: `Header`, `Text`, `Image`, `Button`, `ProductList`, `SocialLinks`, `ProfileHeader`, `CategoryFilter`.
- Reordering: Vertical move up/down within the `LazyColumn`.

### 2. Live Preview
- The editor must provide a "what you see is what you get" (WYSIWYG) experience.
- The center panel renders the page using the same `PageComponentRenderer` used in the public viewer.

### 3. Theme Customization
- Support for 21+ predefined color schemes (e.g., Royal, Ocean, Forest, Cyberpunk).
- Theme changes apply globally to the page and update all components instantly.

### 4. Component Editing
- Selecting a component opens an editor panel (bottom sheet on mobile, side panel on desktop).
- Specific editors for each component type (e.g., `ProfileHeaderComponentEditor` handles name, bio, and image).
- Support for image uploading via the local device.

### 5. Draft Management
- Automatic saving of local drafts to `PageDraftRepository`.
- Explicit "Publish" action to sync the draft with the remote `PageRepository`.
- Option to discard drafts and revert to the last published version.

## Technical Constraints
- Must be responsive (Desktop side-panel vs Mobile bottom-sheet).
- State must be managed via `PageViewModel` and `WhiteLabelState`.
- Persistence must be multiplatform (LocalStorage for Web, SQLite for Server, DataStore/InMemory for others).

## Acceptance Criteria
- Components can be reordered without data loss.
- Images are correctly uploaded to the backend and previewed.
- Theme changes persist across sessions.
- Publishing a page makes it visible at `baseUrl/p/{id}`.
