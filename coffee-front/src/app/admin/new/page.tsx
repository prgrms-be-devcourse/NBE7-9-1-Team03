"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { fetchApi } from "@/lib/client";
import type { ProductDto, FormState } from "@/type/product";


export default function ProductNewPage() {
  const router = useRouter();

  const [form, setForm] = useState<FormState>({
    name: "",
    price: "",     
    stock: "",       
    imageUrl: "",
  });

  // 공통 텍스트 입력
  const onChangeText = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setForm(prev => ({ ...prev, [name]: value }));
  };

  // 숫자 전용 입력: 숫자 외 문자를 제거해 반영
  const onChangeNumeric = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    // 숫자만 남기기
    const onlyDigits = value.replace(/\D+/g, "");
    setForm(prev => ({ ...prev, [name]: onlyDigits }));
  };

  // 숫자 입력에서 e/E/+/- 등의 입력을 막아주는 키다운 가드(크로스 브라우저 안정화)
  const blockNonNumericKeys = (e: React.KeyboardEvent<HTMLInputElement>) => {
    const blocked = ["e", "E", "+", "-", "."];
    if (blocked.includes(e.key)) e.preventDefault();
  };

  const onSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    // 기본 검증
    if (!form.name.trim()) return alert("상품명을 입력하세요.");

    // 빈 문자열이면 0 처리, 아니면 Number 변환
    const priceNum = form.price === "" ? 0 : Number(form.price);
    const stockNum = form.stock === "" ? 0 : Number(form.stock);

    if (Number.isNaN(priceNum) || priceNum < 0) {
      return alert("가격은 0 이상의 숫자만 입력 가능합니다.");
    }
    if (Number.isNaN(stockNum) || stockNum < 0) {
      return alert("재고는 0 이상의 숫자만 입력 가능합니다.");
    }

    // 최종 전송 DTO
    const body = {
      name: form.name.trim(),
      price: priceNum,
      stock: stockNum,
      imageUrl: form.imageUrl.trim() ? form.imageUrl.trim() : undefined,
    };

    try {
      const created = await fetchApi<ProductDto>("/api/products", {
        method: "POST",
        body: JSON.stringify(body),
      });
      alert("상품이 추가되었습니다.");
      // 완료 후 이동 경로는 팀 합의에 맞게
      router.replace(`/admin`); // 또는 router.replace("/product")
    } catch (err: any) {
      alert(err?.message || "상품 추가 실패");
    }
  };

  return (
    <main className="p-4 max-w-xl mx-auto">
      <h1 className="text-2xl font-bold mb-4">상품 추가</h1>

      <form className="flex flex-col gap-3" onSubmit={onSubmit}>
        {/* 상품명 */}
        <input
          className="border rounded p-2"
          name="name"
          placeholder="상품명"
          value={form.name}
          onChange={onChangeText}
          maxLength={100}
        />

        {/* 가격 (숫자만) */}
        <input
          className="border rounded p-2"
          name="price"
          type="text"                 // 문자열로 관리 (placeholder 유지)
          inputMode="numeric"         // 모바일 숫자 키패드 유도
          pattern="\d*"               // 숫자 패턴
          placeholder="가격(원)"
          value={form.price}
          onChange={onChangeNumeric}
          onKeyDown={blockNonNumericKeys}
          maxLength={10}
        />

        {/* 재고 (숫자만) */}
        <input
          className="border rounded p-2"
          name="stock"
          type="text"
          inputMode="numeric"
          pattern="\d*"
          placeholder="재고(개)"
          value={form.stock}
          onChange={onChangeNumeric}
          onKeyDown={blockNonNumericKeys}
          maxLength={9}
        />

        {/* 이미지 URL (일반 텍스트) */}
        <input
          className="border rounded p-2"
          name="imageUrl"
          type="text"
          placeholder="이미지 URL (선택)"
          value={form.imageUrl}
          onChange={onChangeText}
          maxLength={500}
        />

        <button className="bg-blue-600 text-white rounded px-4 py-2">
          저장
        </button>
        <button type="button" className="bg-gray-400 text-white rounded px-4 py-2" onClick={() => router.back()}>
          취소
        </button>
      </form>
    </main>
  );
}
