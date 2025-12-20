import React from "react";
import {Avatar, Dropdown, message} from "antd";
import {UserOutlined} from "@ant-design/icons";

export default function UserMenu({ username }) {
    const handleLogout = () => {
        message.info("Вы вышли из системы");
        localStorage.removeItem("token");
        window.location.reload();
    };

    const menu = {
        items: [
            {
                key: "logout",
                label: "Выход",
                danger: true,
                onClick: handleLogout,
            },
        ],
    };

    return (
        <Dropdown menu={menu} placement="bottomRight">
            <div
                style={{
                    background: "#fff",
                    padding: "4px 12px",
                    borderRadius: "16px",
                    display: "flex",
                    alignItems: "center",
                    cursor: "pointer",
                    gap: "8px",
                    height: "28px",
                }}
            >
                <Avatar size={24} icon={<UserOutlined />} />
                <span style={{ fontWeight: 500 }}>{username}</span>
            </div>
        </Dropdown>
    );
}
