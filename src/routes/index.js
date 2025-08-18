import React, { useState, useEffect } from "react";
import { Route, Routes, useFetcher } from 'react-router-dom';
import { Main, Main2 } from "./pages";

const Router = () => {
    return (
        <div className="app">
            <Routes>
                <Route
                    path="/"
                    element={<Main />}
                />
                <Route
                    path="/main2"
                    element={<Main2 />}
                />
            </Routes>
        </div>
    )
}

/* App/App.js에서 import하기 위한 설정 */
export default Router;