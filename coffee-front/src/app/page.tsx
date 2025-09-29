"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";

const API = process.env.NEXT_PUBLIC_API_BASE ?? "/api";

export default function LoginPage() {
    const router = useRouter();
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    async function handleSubmit(e: React.FormEvent<HTMLFormElement>) {
        e.preventDefault();
        setError(null);

        const EMAIL_RE = new RegExp("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$");
        const emailOk = EMAIL_RE.test(email);
        if (!emailOk) {
            setError("올바른 이메일 형식이 아닙니다.");
            return;
        }

        if(password.length < 8) {
            setError("비밀번호는 8자 이상입니다");
            return;
        }
        setLoading(true);
        try {
            const res = await fetch(`${API}/customer/login`, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                credentials: "include",
                body: JSON.stringify({ email, password }),
            });

            const data = await safeJson(res);
            const resultCode = data?.resultCode ?? data?.code;

            if (!res.ok || resultCode !== "200") {
                const msg = data?.message || data?.msg || `로그인 실패 (${res.status})`;
                throw new Error(msg);
            }

            // ✅ role 초기화 후 새로 저장
        localStorage.removeItem("role");
        const role = data?.data?.role ?? 0; // 0=일반유저, 1=관리자
        localStorage.setItem("role", role.toString());
            if (role === 1) {
            router.replace("/admin");
            } else {
            router.replace("/home");
            }
        } catch (err: any) {
            setError(err?.message ?? "로그인 중 오류가 발생했습니다.");
        } finally {
            setLoading(false);
        }
    }

    return (
        <div className="min-h-[70vh] flex items-center justify-center p-4">
            <div
                className="w-full max-w-md rounded-2xl shadow-lg p-6 bg-white text-gray-900 dark:bg-white dark:text-gray-900">
                <h1 className="text-2xl font-bold mb-6">로그인</h1>
                <p className="text-sm text-gray-500 mb-6">기존 계정으로 로그인 해주세요.</p>

                <form onSubmit={handleSubmit} className="flex flex-col gap-4">
                    <label className="flex flex-col gap-1">
                        <span className="text-sm font-medium">이메일</span>
                        <input
                            type="text"
                            value={email}
                            onChange={(e) => setEmail(e.target.value)}
                            placeholder="example@email.com"
                            required
                            className="border rounded px-3 py-2"
                        />
                    </label>

                    <label className="flex flex-col gap-1">
                        <span className="text-sm font-medium">비밀번호</span>
                        <input
                            type="password"
                            value={password}
                            onChange={(e) => setPassword(e.target.value)}
                            placeholder="비밀번호"
                            required
                            className="border rounded px-3 py-2"
                        />
                    </label>

                    {error && <p className="text-red-600 text-sm">{error}</p>}

                    <button
                        type="submit"
                        disabled={loading}
                        className="w-full rounded-xl bg-black text-white py-2.5 text-sm font-medium hover:opacity-90 disabled:opacity-50"
                    >
                        {loading ? "로그인 중..." : "로그인"}
                    </button>
                </form>
                <p className="mt-6 text-center text-sm text-gray-500">
                    계정이 없나요? <a className="underline" href="/join">회원가입</a>
                </p>
            </div>
        </div>
    );
}

async function safeJson(res: Response) {
    try {
        return await res.json();
    } catch {
        return null;
    }
}
