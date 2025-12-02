import React from "react";
import HeaderBar from "./HeaderBar";
import {Layout} from "antd";

const { Header, Content } = Layout;

export default function AppLayout({ children }) {
    return (
        <Layout style={{ height: "100vh" }}>
            <HeaderBar />
            <Content style={{ padding: 20, overflowY: "auto" }}>
                {children}
            </Content>
        </Layout>
    );
}
