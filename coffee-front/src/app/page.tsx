"use client";

import { useEffect, useMemo, useRef, useState } from "react";
import { fetchApi } from "@/lib/client";
import type { ProductDto, CartItem } from "@/type/product";
import type { OrderRequest } from "@/type/order";
import type { CustomerDto } from "@/type/customer";

export default function Home() {
  // 상품 목록
  const [products, setProducts] = useState<ProductDto[] | null>(null);
  const [listError, setListError] = useState<string | null>(null);

  // 장바구니
  const [cart, setCart] = useState<CartItem[]>([]);

  // 토스트 알림
  const [toast, setToast] = useState<{ message: string; type: 'success' | 'error' } | null>(null);

  // 사용자 정보
  const emailRef = useRef<HTMLInputElement>(null);
  const [customer, setCustomer] = useState<CustomerDto | null>(null);
  const [form, setForm] = useState<CustomerDto | null>(null);
  const [editMode, setEditMode] = useState(false);

  // 토스트 표시 함수
  const showToast = (message: string, type: 'success' | 'error' = 'success') => {
    setToast({ message, type });
    setTimeout(() => setToast(null), 3000);
  };

  // 1) 상품 목록 조회
  useEffect(() => {
    fetchApi<ProductDto[]>(`/api/products`)
      .then(setProducts)
      .catch((e) => {
        setListError(e.message || "상품 목록 조회 실패");
      });
  }, []);

  //
  


 // 사용자 정보 불러오기
 const loadCustomer = async () => {
  try {
    const res = await fetchApi<{ data: { customerDto: CustomerDto } }>(`/api/customer/me`);
    const dto = res.data.customerDto;
    setCustomer(dto);
    setForm(dto);
  } catch (e: any) {
    console.error("사용자 정보 조회 실패:", e);
  }
};

useEffect(() =>{
  loadCustomer();
},[]);




  // 2) 장바구니 조회
  const loadCart = async () => {
    const email = form?.email;
    if (!email) {
      alert("로그인을 해주세요.");
      return;
    }

    try {
      type CartApiItem = {
        productId: number;
        quantity: number;
      };
      type CartResponse = {
        resultCode: string;
        msg: string;
        data: CartApiItem[];
      };
      const response = await fetchApi<CartResponse>(`/api/cart/${email}`);
      
      // API에서 받은 장바구니 데이터와 상품 목록을 매칭
      const cartData = response.data || [];
      const validatedCart: CartItem[] = cartData
        .map(item => {
          // products 배열에서 해당 productId를 가진 상품 찾기
          const product = products?.find(p => p.id === item.productId);
          
          if (!product) {
            console.warn(`상품 ID ${item.productId}를 찾을 수 없습니다.`);
            return null;
          }
          
          return {
            productId: item.productId,
            name: product.name,
            price: product.price,
            quantity: item.quantity || 1,
          };
        })
        .filter((item): item is CartItem => item !== null);
      
      setCart(validatedCart);
    } catch (e: any) {
      console.error("장바구니 조회 실패:", e);
      setCart([]);
    }
  };

  // 3) 장바구니에 상품 추가
  const addToCart = async (p: ProductDto) => {
    const email = form?.email;
   
    // 이미 장바구니에 있는 경우 → 수량 증가
    const existing = cart.find(c => c.productId === p.id);
    if (existing) {
      await updateQty(p.id, (existing.quantity || 1) + 1);
      return;
    }

    try {
      type CartAddResponse = {
        resultCode: string;
        msg: string;
      };

      await fetchApi<CartAddResponse>(`/api/cart/add`, {
        method: "POST",
        body: JSON.stringify({
          customerEmail: email,
          productId: p.id,
          quantity: 1,
        }),
      });

      // 장바구니 다시 조회
      await loadCart();
      showToast("장바구니에 추가되었습니다.");
    } catch (e: any) {
      showToast(e.message || "장바구니 추가 실패", 'error');
    }
  };

  // 4) 장바구니 상품 삭제
  const removeFromCart = async (productId: number) => {
    const email = form?.email;
    if (!email) {
      alert("로그인을 해주세요.");
      return;
    }

    try {
      type CartRemoveResponse = {
        resultCode: string;
        msg: string;
      };

      await fetchApi<CartRemoveResponse>(`/api/cart/remove`, {
        method: "DELETE",
        body: JSON.stringify({
          customerEmail: email,
          productId: productId,
        }),
      });

      // 장바구니 다시 조회
      await loadCart();
      showToast("상품이 삭제되었습니다.");
    } catch (e: any) {
      showToast(e.message || "장바구니 삭제 실패", 'error');
    }
  };

  // 5) 장바구니 전체 비우기
  const clearCart = async () => {
    const email = form?.email;
    if (!email) {
      alert("로그인을 해주세요.");
      return;
    }

    if (!confirm("장바구니를 전체 비우시겠습니까?")) return;

    try {
      type CartClearResponse = {
        resultCode: string;
        msg: string;
      };

      await fetchApi<CartClearResponse>(`/api/cart/clear`, {
        method: "DELETE",
        body: JSON.stringify({
          customerEmail: email,
        }),
      });

      setCart([]);
      showToast("장바구니가 비워졌습니다.");
    } catch (e: any) {
      showToast(e.message || "장바구니 비우기 실패", 'error');
    }
  };

  // 6) 수량 수정 (장바구니 재추가 방식)
  const updateQty = async (productId: number, nextQty: number) => {
    const email = form?.email;
    if (!email) {
      alert("로그인을 해주세요.");
      return;
    }

    if (nextQty <= 0) {
      await removeFromCart(productId);
      return;
    }

    try {
      await fetchApi(`/api/cart/add`, {
        method: "POST",
        body: JSON.stringify({
          customerEmail: email,
          productId: productId,
          quantity: nextQty,
        }),
      });

      await loadCart();
    } catch (e: any) {
      showToast(e.message || "수량 수정 실패", 'error');
    }
  };

  const totalPrice = useMemo(
    () => cart.reduce((sum, c) => sum + (c.price || 0) * (c.quantity || 0), 0),
    [cart]
  );

  // 7) 주문서 제출
  const submitOrder = async (e: React.FormEvent) => {
    e.preventDefault();

    const customerEmail = form?.email || "";

    if (cart.length === 0) {
      alert("장바구니가 비어있습니다. 상품을 추가해주세요.");
      return;
    }
    if (customerEmail.length === 0) {
      alert("로그인을 해주세요.");
      emailRef.current?.focus();
      return;
    }

    const body: OrderRequest = cart.map(c => ({
      customerEmail: customerEmail,
      productId: c.productId,
      quantity: c.quantity,
    }));

    try {
      type OrderResponse = { msg: string };

      const data = await fetchApi<OrderResponse>(`/api/orders`, {
        method: "POST",
        body: JSON.stringify(body),
      });
      alert(data.msg || "주문이 완료되었습니다.");
      
      // 주문 완료 후 장바구니 비우기
      await clearCart();
    } catch (e: any) {
      alert(e.message || "주문에 실패했습니다.");
    }
  };

  // 주소/우편번호 수정
  const saveCustomer = async () => {
    if (!form) return;
    try {
      await fetchApi(`/api/customer/address`, {
        method: "PUT",
        body: JSON.stringify(form),
      });
      setCustomer(form);
      setEditMode(false);
      showToast("정보가 수정되었습니다.");
    } catch (e: any) {
      showToast(e.message || "정보 수정 실패", 'error');
    }
  };

  

  return (
    <main className="p-4 max-w-7xl mx-auto">
      <h1 className="text-2xl font-bold mb-6">메인 페이지</h1>

      {/* 토스트 알림 */}
      {toast && (
        <div className="fixed top-4 right-4 z-50 animate-slide-in">
          <div className={`px-6 py-3 rounded-lg shadow-lg ${
            toast.type === 'success' 
              ? 'bg-green-500 text-white' 
              : 'bg-red-500 text-white'
          }`}>
            {toast.message}
          </div>
        </div>
      )}

      <div className="flex gap-6">
        {/* 왼쪽: 상품 목록 */}
        <section className="flex-1">
          <h2 className="text-xl font-semibold mb-4">상품 목록</h2>

          {products === null && !listError && <div>Loading...</div>}
          {listError && <div className="text-red-600">{listError}</div>}

          {products !== null && products.length === 0 && (
            <div>상품이 없습니다.</div>
          )}

          {products !== null && products.length > 0 && (
            <ul className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
              {products.map((p) => (
                <li key={p.id} className="border rounded p-3 flex flex-col gap-2">
                  <img src={p.imageUrl} alt={p.name} className="w-full h-48 object-cover rounded" />
                  <div className="font-semibold">{p.name}</div>
                  <div className="font-bold text-lg">
                    {p.price.toLocaleString()}원
                  </div>
                  <button 
                    className="border rounded px-3 py-2 hover:bg-blue-50 hover:border-blue-500 transition-colors" 
                    onClick={() => addToCart(p)}
                  >
                    장바구니 담기
                  </button>
                </li>
              ))}
            </ul>
          )}
        </section>

        {/* 오른쪽: 장바구니 (고정) */}
        <aside className="w-96 flex-shrink-0">
          <div className="sticky top-4">
            <div className="border rounded-lg p-4 bg-gray-50">
              <div className="flex justify-between items-center mb-4">
                <h2 className="text-xl font-semibold">장바구니</h2>
                {cart.length > 0 && (
                  <button
                    type="button"
                    className="text-sm text-red-600 hover:underline"
                    onClick={clearCart}
                  >
                    전체 삭제
                  </button>
                )}
              </div>

              {/* 이메일 입력 & 장바구니 조회 */}
              <div className="mb-4 flex gap-2">
                <button
                  type="button"
                  className="border rounded px-3 py-2 bg-white hover:bg-gray-100"
                  onClick={loadCart}
                >
                  조회
                </button>
                {!editMode ? (
              <button onClick={() => setEditMode(true)} className="border px-3 py-2 rounded">주소 수정</button>
            ) : (
              <>
                <button onClick={saveCustomer} className="border px-3 py-2 rounded">완료</button>
                <button onClick={() => { setForm(customer); setEditMode(false); }} className="border px-3 py-2 rounded">취소</button>
              </>
            )}
              </div>
              {/* 주소 & 우편번호 */}
              <div className="mb-4">
            <input
              type="text"
              className="w-full border rounded p-2 mb-2"
              value={form?.address || ""}
              onChange={(e) => setForm({ ...(form as CustomerDto), address: e.target.value })}
              placeholder="주소"
              disabled={!editMode}
            />
            <input
              type="text"
              className="w-full border rounded p-2"
              value={form?.postalCode || ""}
              onChange={(e) => setForm({ ...(form as CustomerDto), postalCode: Number(e.target.value) })}
              placeholder="우편번호"
              disabled={!editMode}
            />
          </div>

              {/* 장바구니 상품 목록 */}
              <div className="bg-white border rounded p-3 mb-4 max-h-96 overflow-y-auto">
                {cart.length === 0 && (
                  <div className="text-gray-500 text-center py-8">
                    장바구니가 비어있습니다
                  </div>
                )}

                {cart.length > 0 && (
                  <ul className="flex flex-col gap-3">
                    {cart.map((c) => (
                      <li
                        key={c.productId}
                        className="flex flex-col gap-2 border-b pb-3 last:border-b-0"
                      >
                        <div className="font-medium">{c.name || '알 수 없는 상품'}</div>
                        <div className="text-sm text-gray-600">
                          {(c.price || 0).toLocaleString()}원
                        </div>

                        <div className="flex items-center justify-between">
                          <div className="flex items-center gap-1">
                            <button
                              type="button"
                              className="border rounded px-2 py-1 hover:bg-gray-100"
                              onClick={() => updateQty(c.productId, (c.quantity || 1) - 1)}
                            >
                              -
                            </button>
                            <input
                              className="w-12 border rounded p-1 text-center"
                              type="number"
                              min={1}
                              value={c.quantity || 1}
                              onChange={(e) =>
                                updateQty(c.productId, Number(e.target.value) || 1)
                              }
                            />
                            <button
                              type="button"
                              className="border rounded px-2 py-1 hover:bg-gray-100"
                              onClick={() => updateQty(c.productId, (c.quantity || 1) + 1)}
                            >
                              +
                            </button>
                          </div>

                          <button
                            type="button"
                            className="text-red-600 text-sm hover:underline"
                            onClick={() => removeFromCart(c.productId)}
                          >
                            삭제
                          </button>
                        </div>

                        <div className="text-right font-semibold">
                          {((c.price || 0) * (c.quantity || 0)).toLocaleString()}원
                        </div>
                      </li>
                    ))}
                  </ul>
                )}
              </div>

              {/* 합계 */}
              <div className="bg-white border rounded p-3 mb-4">
                <div className="flex justify-between items-center text-lg font-bold">
                  <span>총 금액</span>
                  <span className="text-blue-600">{totalPrice.toLocaleString()}원</span>
                </div>
              </div>

              {/* 주문 폼 */}
              <form onSubmit={submitOrder} className="flex flex-col gap-3">
                <button
                  className="bg-blue-600 text-white rounded px-4 py-3 font-semibold hover:bg-blue-700 transition-colors disabled:bg-gray-400"
                  type="submit"
                  disabled={cart.length === 0}
                >
                  주문하기
                </button>
              </form>
            </div>
          </div>
        </aside>
      </div>
    </main>
  );
}