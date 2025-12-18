import React from "react";
import HeaderBar from "./HeaderBar";
import {Layout} from "antd";

const { Header, Content } = Layout;

export default function AppLayout({ children }) {
    return (
        <Layout style={{ minHeight: "100vh" }}>
            <HeaderBar />
            <Content
                style={{
                    padding: 20,
                    maxWidth: 1200,
                    margin: "0 auto",
                    width: "100%",
                }}
            >
                {children}
            </Content>
        </Layout>
    );
}
