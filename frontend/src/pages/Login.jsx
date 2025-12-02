import React, {useState} from "react";
import {Button, Card, Form, Input, message} from "antd";
import api from "../api/axios";

export default function Login() {
    const [loading, setLoading] = useState(false);
    const [regLoading, setRegLoading] = useState(false);
    const [form] = Form.useForm();

    const loginUser = async (values) => {
        setLoading(true);
        try {
            const res = await api.post("/reactive/user/login", values);
            const tokenHeader = res.headers.getAuthorization?.();
            if (tokenHeader?.startsWith("Bearer ")) {
                const token = tokenHeader.substring("Bearer ".length);
                localStorage.setItem("token", token);
                message.success("Успешный вход!");
                window.location.href = "/";
            } else {
                message.error("Токен не найден в ответе");
            }
        } catch {
            message.error("Ошибка авторизации");
        } finally {
            setLoading(false);
        }
    };

    const onFinish = async (values) => {
        await loginUser(values);
    };

    const onRegister = async () => {
        const values = form.getFieldsValue();

        if (!values.login || !values.password) {
            message.warning("Введите логин и пароль перед регистрацией");
            return;
        }

        setRegLoading(true);
        try {
            await api.post("/reactive/user/register", values);
            message.success("Регистрация успешна! Выполняется вход...");
            await loginUser(values);
        } catch (e) {
            message.error(
                "Ошибка регистрации: " +
                    (e?.response?.data?.message || "Неизвестная ошибка")
            );
        } finally {
            setRegLoading(false);
        }
    };

    return (
        <div
            style={{
                display: "flex",
                justifyContent: "center",
                alignItems: "center",
                height: "100vh",
                backgroundColor: "#f0f2f5",
            }}
        >
            <Card title="Авторизация" style={{ width: 300 }}>
                <Form form={form} onFinish={onFinish}>
                    <Form.Item
                        name="login"
                        rules={[{ required: true, message: "Введите логин" }]}
                    >
                        <Input placeholder="Логин" />
                    </Form.Item>

                    <Form.Item
                        name="password"
                        rules={[{ required: true, message: "Введите пароль" }]}
                    >
                        <Input.Password placeholder="Пароль" />
                    </Form.Item>

                    <Button
                        type="primary"
                        htmlType="submit"
                        loading={loading}
                        block
                    >
                        Войти
                    </Button>

                    <Button
                        style={{ marginTop: 10 }}
                        loading={regLoading}
                        block
                        onClick={onRegister}
                    >
                        Регистрация
                    </Button>
                </Form>
            </Card>
        </div>
    );
}
