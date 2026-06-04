import { Navigate, Outlet } from "react-router-dom";
import { loadCurrentUser } from "../auth";

function RequireRole({ roles }) {
  const currentUser = loadCurrentUser();

  if (!currentUser) {
    return <Navigate replace to="/" />;
  }

  if (!roles.includes(currentUser.role)) {
    return <Navigate replace to="/" />;
  }

  return <Outlet />;
}

export default RequireRole;
