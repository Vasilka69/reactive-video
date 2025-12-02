import React, {useEffect, useState} from "react";
import UserMenu from "./UserMenu";
import {fetchUser} from "../../api/user";

export default function HeaderBar() {
    const [user, setUser] = useState(null);

    useEffect(() => {
        async function load() {
            try {
                const data = await fetchUser();
                setUser(data);
            } catch (e) {
                console.error("Ошибка загрузки пользователя", e);
            }
        }
        load();
    }, []);

    return (
        <div
            style={{
                width: "100%",
                height: "40px",
                background: "#2d7eefff",
                display: "flex",
                alignItems: "center",
                justifyContent: "flex-end",
                paddingRight: "20px",
                boxSizing: "border-box",
            }}
        >
            <UserMenu username={user?.login || "..."} />
        </div>
    );
}
