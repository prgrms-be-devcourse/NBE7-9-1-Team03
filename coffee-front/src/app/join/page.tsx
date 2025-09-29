"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";

const API = process.env.NEXT_PUBLIC_API_BASE ?? "/api";

async function safeJson(res: Response) {
    try { return await res.json(); } catch { return null; }
}

export default function SignupPage() {
    const router = useRouter();

    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [username, setUsername] = useState("");
    const [role, setRole] = useState("");
    const [address, setAddress] = useState("");
    const [postalCode, setPostalCode] = useState("");

    const [error, setError] = useState<string | null>(null);
    const [loading, setLoading] = useState(false);

    async function handleSubmit(e: React.FormEvent<HTMLFormElement>) {
        e.preventDefault();
        setError(null);

        if (password.length < 8) {
            setError("비밀번호는 8자 이상이어야합니다.");
            return;
        }

        const EMAIL_RE = new RegExp("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$");
        const emailOk = EMAIL_RE.test(email);
        if (!emailOk) {
            setError("올바른 이메일 형식이 아닙니다.");
            return;
        }

        if (!/^[가-힣a-zA-Z0-9\s-]{5,100}$/.test(address)) {
            setError("주소는 한글, 영문, 숫자, 공백, 하이픈(-)만 가능하며 5~100자 이내여야 합니다.");
            return;
        }

        if (!/^\d{5}$/.test(postalCode)) {
            setError("우편번호는 숫자 5자리여야 합니다.");
            return;
        }

        setLoading(true);
        try {
            const res = await fetch(`${API}/customer/join`, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                credentials: "include",
                body: JSON.stringify({ email, password, username, address, postalCode }),
            });

            const data = await safeJson(res);
            if (!res.ok || data?.code !== "201") {
                const msg = data?.message || data?.msg || `회원가입 실패 (${res.status})`;
                throw new Error(msg);
            }
            router.replace("/login");
        } catch (err: any) {
            setError(err?.message ?? "회원가입 중 오류가 발생했습니다.");
        } finally {
            setLoading(false);
        }
    }

    return (
        <div className="min-h-[70vh] flex items-center justify-center p-4">
            <div className="w-full max-w-md rounded-2xl shadow-lg p-6 bg-white text-gray-900 dark:bg-white dark:text-gray-900">
                <h1 className="text-2xl font-semibold mb-1">회원가입</h1>
                <p className="text-sm text-gray-500 mb-6">이메일로 새 계정을 만들어 보세요.</p>

                {error && (
                    <div className="mb-4 rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-600">
                        {error}
                    </div>
                )}

                <form onSubmit={handleSubmit} className="space-y-4">
                    <div>
                        <label htmlFor="email" className="block text-sm font-medium mb-1 text-gray-700">이메일</label>
                        <input id="email" type="text" value={email}
                               onChange={(e) => setEmail(e.target.value)}
                               className="w-full rounded-xl border px-3 py-2 outline-none focus:ring-2 focus:ring-black/10 bg-white text-gray-900 placeholder:text-gray-400"
                               placeholder="you@example.com" required autoComplete="email"/>
                    </div>

                    <div>
                        <label htmlFor="username" className="block text-sm font-medium mb-1 text-gray-700">사용자
                            이름</label>
                        <input id="username" type="text" value={username}
                               onChange={(e) => setUsername(e.target.value)}
                               className="w-full rounded-xl border px-3 py-2 outline-none focus:ring-2 focus:ring-black/10 bg-white text-gray-900 placeholder:text-gray-400"
                               placeholder="별명 또는 이름" required/>
                    </div>

                    <div>
                        <label htmlFor="password" className="block text-sm font-medium mb-1 text-gray-700">비밀번호</label>
                        <input id="password" type="password" value={password}
                               onChange={(e) => setPassword(e.target.value)}
                               className="w-full rounded-xl border px-3 py-2 outline-none focus:ring-2 focus:ring-black/10 bg-white text-gray-900 placeholder:text-gray-400"
                               placeholder="8자 이상" required autoComplete="new-password"/>
                    </div>

                    <div>
                        <label htmlFor="address" className="block text-sm font-medium mb-1 text-gray-700">주소</label>
                        <input id="address" type="text" value={address}
                               onChange={(e) => setAddress(e.target.value)}
                               className="w-full rounded-xl border px-3 py-2 outline-none focus:ring-2 focus:ring-black/10 bg-white text-gray-900 placeholder:text-gray-400"
                               placeholder="거주지 주소" required/>
                    </div>

                    <div>
                        <label htmlFor="postalCode"
                               className="block text-sm font-medium mb-1 text-gray-700">우편번호</label>
                        <input id="postalCode" type="text" value={postalCode}
                               onChange={(e) => setPostalCode(e.target.value)}
                               className="w-full rounded-xl border px-3 py-2 outline-none focus:ring-2 focus:ring-black/10 bg-white text-gray-900 placeholder:text-gray-400"
                               placeholder="우편번호" required/>
                    </div>

                    <button type="submit" disabled={loading}
                            className="w-full rounded-xl bg-black text-white py-2.5 text-sm font-medium hover:opacity-90 disabled:opacity-50">
                        {loading ? "처리 중..." : "회원가입"}
                    </button>
                </form>

                <p className="mt-6 text-center text-sm text-gray-500">
                    이미 계정이 있나요? <a className="underline" href="/">로그인</a>
                </p>
            </div>
        </div>
    );
}
