## Team Workflow

This repository follows a structured branch workflow to keep development organized, stable, and easy to collaborate on.

---

### Branch Structure

| Branch | Purpose |
|------|--------|
| **`main`** | Final, stable version of the app. Always runnable. Used for grading and demos. **Protected.** |
| **`dev`** | Team integration branch where completed work is merged and tested together. |
| **`work/*`** | Personal workspaces for each team member. |

**Personal branches:**
- `work/christy`
- `work/member2`
- `work/member3`
- `work/member4`

---

### Development Flow

1. **Start from `dev`**
   - Ensure `dev` is up to date before beginning work.

2. **Work in your personal branch**
   - Switch to your assigned `work/<name>` branch.
   - Commit changes freely while developing.

3. **Open a Pull Request**
   - When ready, open a PR:
     - **From:** `work/<name>`
     - **Into:** `dev`
   - Include a brief description of your changes.

4. **Review & Merge**
   - Pull requests are reviewed for build stability and conflicts.
   - Approved PRs are merged into `dev`.

5. **Release to `main`**
   - When `dev` is stable and ready for submission or demo, it is merged into `main`.

