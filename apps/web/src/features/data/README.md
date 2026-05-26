This folder is reserved for feature-local static data or fixtures.

The dashboard shell now reads through `features/app-shell/dashboardService.ts` so that mock data and real local API access can share the same consumer boundary.
