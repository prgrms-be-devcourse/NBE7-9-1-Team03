"use client";

import Link from "next/link";
import {useState} from "react";

export default function QuitDonePage() {
    const [status, setStatus] = useState<"loading" | "success" | "error">("loading");
    const [msg, setMsg] = useState<string>("");
    return (
        <div className="min-h-[60vh] flex items-center justify-center p-6">
            <div className="w-full max-w-md rounded-2xl shadow-lg p-6 bg-white text-gray-900">
                <h1 className="text-2xl font-semibold mb-4">회원 탈퇴</h1>

                <>
                    <p className = "mb-4 text-green-600">{msg || "회원 탈퇴가 완료되었습니다."}</p>
                    <Link href="/login" className="underline">
                            로그인 페이지로 돌아가기
                    </Link>
                </>
            </div>
        </div>
    );
}

