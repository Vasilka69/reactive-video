import React from "react";
import {Navigate, Route, Routes} from "react-router-dom";
import Login from "../pages/Login";
import Home from "../pages/Home";
import NotFound from "../pages/NotFound";

// компонент для защищённых маршрутов
const PrivateRoute = ({ children }) => {
    const token = localStorage.getItem("token"); // берём токен из localStorage
    return token ? children : <Navigate to="/login" replace />;
};

export default function AppRoutes() {
    return (
        <Routes>
            <Route path="/login" element={<Login />} />
            <Route
                path="/"
                element={
                    <PrivateRoute>
                        <Home />
                    </PrivateRoute>
                }
            />
            <Route path="*" element={<NotFound />} />
        </Routes>
    );
}
