"use client";

import { useEffect, useMemo, useRef, useState } from "react";
import { fetchApi } from "@/lib/client";
import type { ProductDto, CartItem } from "@/type/product";
import type { OrderRequest } from "@/type/order";

export default function Home() {
  // 상품 목록
  const [products, setProducts] = useState<ProductDto[] | null>(null);
  const [listError, setListError] = useState<string | null>(null);

  // 주문서(장바구니)
  const [cart, setCart] = useState<CartItem[]>([]);

  // 사용자 정보
  const emailRef = useRef<HTMLInputElement>(null);
  // const addressRef = useRef<HTMLInputElement>(null); // OrderDTO에 주소 및 우편 번호 없음
  // const zipcodeRef = useRef<HTMLInputElement>(null);

  // 1) 상품 목록 조회
  useEffect(() => {
    fetchApi<ProductDto[]>(`/products`)
      .then(setProducts)
      .catch((e) => {
        setListError(e.message || "상품 목록 조회 실패");
      });
  }, []);

  // 1-1) 장바구니에 상품 추가
  const addToCart = (p: ProductDto) => {
    setCart((prev) => {
      const found = prev.find((c) => c.productId === p.id);
      if (found) {
        // 이미 있으면 수량 +1
        return prev.map((c) =>
          c.productId === p.id ? { ...c, quantity: c.quantity + 1 } : c
        );
      }
      // 없으면 신규 추가
      return [
        ...prev,
        { productId: p.id, name: p.name, price: p.price, quantity: 1 },
      ];
    });
  };

  // 1-2) 장바구니 수량 수정/삭제
  const updateQty = (productId: number, nextQty: number) => {
    if (nextQty <= 0) {
      // 0 이하로 내려가면 삭제
      setCart((prev) => prev.filter((c) => c.productId !== productId));
      return;
    }
    setCart((prev) =>
      prev.map((c) =>
        c.productId === productId ? { ...c, quantity: nextQty } : c
      )
    );
  };
  const removeItem = (productId: number) => {
    setCart((prev) => prev.filter((c) => c.productId !== productId));
  };

  const totalPrice = useMemo(
    () => cart.reduce((sum, c) => sum + c.price * c.quantity, 0),
    [cart]
  );

  // 2-2) 주문서 제출 → POST /orders
  const submitOrder = async (e: React.FormEvent) => {
    e.preventDefault();

    const customerEmail = emailRef.current?.value?.trim() || "";
    // const address = addressRef.current?.value?.trim() || "";  
    // const zipcode = zipcodeRef.current?.value?.trim() || "";

    if (cart.length === 0) {
      alert("주문서가 비어있습니다. 상품을 추가해주세요.");
      return;
    }
    if (customerEmail.length === 0) {
      alert("이메일을 입력해주세요.");
      emailRef.current?.focus();
      return;
    }
    // if (address.length === 0) {
    //   alert("주소를 입력해주세요.");
    //   addressRef.current?.focus();
    //   return;
    // }
    // if (zipcode.length === 0) {
    //   alert("우편번호를 입력해주세요.");
    //   zipcodeRef.current?.focus();
    //   return;
    // }

    const body: OrderRequest = cart.map(c => ({
      customerEmail: customerEmail,
      productId: c.productId,
      quantity: c.quantity,
    }));

    try {
      type OrderResponse = { msg: string };

      const data = await fetchApi<OrderResponse>(`/orders`, {
        method: "POST",
        body: JSON.stringify(body),
      });
      alert(data.msg || "주문이 완료되었습니다.");
      // 주문 완료 후 초기화
      setCart([]);
      if (emailRef.current) emailRef.current.value = "";
      // if (addressRef.current) addressRef.current.value = "";
      // if (zipcodeRef.current) zipcodeRef.current.value = "";
    } catch (e: any) {
      alert(e.message || "주문에 실패했습니다.");
    }
  };

  return (
    <main className="p-4 max-w-6xl mx-auto">
      <h1 className="text-2xl font-bold mb-4">메인 페이지</h1>

      {/* 1) 상품 목록 영역 */}
      <section className="mb-8">
        <h2 className="text-xl font-semibold mb-3">상품 목록</h2>

        {products === null && !listError && <div>Loading...</div>}
        {listError && <div className="text-red-600">{listError}</div>}

        {products !== null && products.length === 0 && (
          <div>상품이 없습니다.</div>
        )}

        {products !== null && products.length > 0 && (
          <ul className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
            {products.map((p) => (
              <li key={p.id} className="border rounded p-3 flex flex-col gap-2">
                <img src={p.imageUrl} />
                <div className="font-semibold">{p.name}</div>
                <div className="font-bold">
                  {p.price.toLocaleString()}원
                </div>
                <button
                  className="border rounded px-3 py-2 hover:bg-gray-50"
                  onClick={() => addToCart(p)}
                >
                  추가
                </button>
              </li>
            ))}
          </ul>
        )}
      </section>

      {/* 2) 주문서 영역 */}
      <section>
        <h2 className="text-xl font-semibold mb-3">주문서</h2>

        {/* 2-1) 사용자 정보 입력 */}
        <form className="flex flex-col gap-3 mb-4" onSubmit={submitOrder}>
          <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
            <input
              ref={emailRef}
              className="border rounded p-2"
              type="email"
              placeholder="이메일"
              maxLength={50}
            />
            {/* <input
              ref={addressRef}
              className="border rounded p-2"
              type="text"
              placeholder="주소"
              maxLength={100}
            />
            <input
              ref={zipcodeRef}
              className="border rounded p-2"
              type="text"
              placeholder="우편번호"
              maxLength={10}
            /> */}
          </div>

          {/* 1-2) 장바구니: 수량 수정/삭제 */}
          <div className="border rounded p-3">
            <h3 className="font-semibold mb-2">추가된 상품</h3>

            {cart.length === 0 && <div>아직 추가된 상품이 없습니다.</div>}

            {cart.length > 0 && (
              <ul className="flex flex-col gap-2">
                {cart.map((c) => (
                  <li
                    key={c.productId}
                    className="flex items-center gap-2 border-b pb-2"
                  >
                    <span className="flex-1">
                      {c.name} · {c.price.toLocaleString()}원
                    </span>

                    <div className="flex items-center gap-1">
                      <button
                        type="button"
                        className="border rounded px-2"
                        onClick={() => updateQty(c.productId, c.quantity - 1)}
                      >
                        -
                      </button>
                      <input
                        className="w-14 border rounded p-1 text-center"
                        type="number"
                        min={1}
                        value={c.quantity}
                        onChange={(e) =>
                          updateQty(c.productId, Number(e.target.value) || 1)
                        }
                      />
                      <button
                        type="button"
                        className="border rounded px-2"
                        onClick={() => updateQty(c.productId, c.quantity + 1)}
                      >
                        +
                      </button>
                    </div>

                    <button
                      type="button"
                      className="border rounded px-3 py-1"
                      onClick={() => removeItem(c.productId)}
                    >
                      삭제
                    </button>
                  </li>
                ))}
              </ul>
            )}

            <div className="text-right mt-3 font-bold">
              합계: {totalPrice.toLocaleString()}원
            </div>
          </div>

          {/* 2-2) 주문 제출 */}
          <div className="text-right">
            <button
              className="bg-blue-600 text-white rounded px-4 py-2"
              type="submit"
            >
              주문하기
            </button>
          </div>
        </form>
      </section>
    </main>
  );
}
