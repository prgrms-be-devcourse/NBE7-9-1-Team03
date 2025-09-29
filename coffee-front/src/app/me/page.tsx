"use client";

import { useEffect, useState } from "react";

const API = process.env.NEXT_PUBLIC_API_BASE ?? "/api";

import { useRouter } from "next/navigation";

type CustomerDto = {
  email: string;
  username: string;
  address: string;
  postalCode: number;
};

export default function MyPage() {
  const [customer, setCustomer] = useState<CustomerDto | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const [editMode, setEditMode] = useState(false);
  const [form, setForm] = useState<CustomerDto | null>(null);
  const [password, setPassword] = useState("");
  const router = useRouter();

  // GET /customer/me
  const load = async () => {
    setLoading(true);
    try {
      const res = await fetch(`${API}/customer/me`, {
        method: "GET",
        credentials: "include",
      });
      if (!res.ok) throw new Error(`(${res.status}) 조회 실패`);
      const data = await res.json();
      const dto: CustomerDto = data.data.customerDto;
      setCustomer(dto);
      setForm(dto);
    } catch (err: any) {
      setError(err.message ?? "내 정보 불러오기 실패");
    } finally {
      setLoading(false);
    }
  };

  // PUT /customer/me
  const save = async () => {
    if (!form) return;
    try {
      const res = await fetch(`${API}/customer/me`, {
        method: "PUT",
        credentials: "include",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          ...form,
          password,
        }),
      });
      // 본문 한 번만 읽기
    const data = await res.json().catch(() => null);

    // HTTP 실패 또는 비즈니스 코드 실패 모두 처리
    if (!res.ok || data?.resultCode !== "200") {
      if (data?.msg?.includes("비밀번호")) {
        throw new Error("비밀번호가 일치하지 않습니다");
      }
      throw new Error(data?.msg || `(${res.status}) 요청 실패`);
    }
      await load();
      setEditMode(false);
      alert("수정되었습니다.");
    } catch (err: any) {
      alert(err.message || "수정 실패");
    }
  };

  useEffect(() => {
    load();
  }, []);

  if (loading) return <div>Loading...</div>;
  if (error) return <div className="text-red-500">{error}</div>;
  if (!customer || !form) return <div>데이터 없음</div>;

  return (
    <div className="max-w-md mx-auto p-6 bg-white text-black rounded-xl shadow-md">
      <h1 className="text-2xl font-semibold mb-4">마이페이지</h1>

      {!editMode ? (
          <>
            <p><strong>이메일:</strong> {customer.email}</p>
            <p><strong>이름:</strong> {customer.username}</p>
            <p><strong>주소:</strong> {customer.address}</p>
            <p><strong>우편번호:</strong> {customer.postalCode}</p>
            <div className="flex items-center gap-2">
              <button
                  className="mt-4 px-4 py-2 bg-gray-800 text-white rounded"
                  onClick={() => setEditMode(true)}
              >
                수정
              </button>
              <button
                  className="mt-4 px-4 py-2 bg-gray-800 text-white rounded"
                  onClick={() => router.push("/quit")}
              >
                회원탈퇴
              </button>
              <button
                  className="mt-4 px-4 py-2 bg-gray-800 text-white rounded"
                  onClick={() => router.push("/home")}
              >
                뒤로가기
              </button>
            </div>
          </>
      ) : (
          <div className="space-y-3">
            <div>
              <label className="block text-sm font-medium">이메일 (변경 불가)</label>
              <input
                  type="email"
                  value={form.email}
                  readOnly
                  className="w-full border px-3 py-2 rounded bg-gray-100 text-gray-500"
              />
            </div>
          <div>
            <label className="block text-sm font-medium">비밀번호 (확인용)</label>
            <input
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              className="w-full border px-3 py-2 rounded"
              placeholder="현재 비밀번호"
              required
            />
          </div>
          <div>
            <label className="block text-sm font-medium">이름</label>
            <input
              type="text"
              value={form.username}
              onChange={(e) => setForm({ ...form, username: e.target.value })}
              className="w-full border px-3 py-2 rounded"
            />
          </div>
          <div>
            <label className="block text-sm font-medium">주소</label>
            <input
              type="text"
              value={form.address}
              onChange={(e) => setForm({ ...form, address: e.target.value })}
              className="w-full border px-3 py-2 rounded"
            />
          </div>
          <div>
            <label className="block text-sm font-medium">우편번호</label>
            <input
              type="text"
              value={form.postalCode}
              onChange={(e) =>
                setForm({ ...form, postalCode: Number(e.target.value) })
              }
              className="w-full border px-3 py-2 rounded"
            />
          </div>
          <div className="flex gap-2">
            <button
              className="px-4 py-2 bg-blue-600 text-white rounded"
              onClick={save}
            >
              저장
            </button>
            <button
              className="px-4 py-2 bg-gray-400 text-white rounded"
              onClick={() => setEditMode(false)}
            >
              취소
            </button>
          </div>
        </div>
      )}
    </div>
  );
}