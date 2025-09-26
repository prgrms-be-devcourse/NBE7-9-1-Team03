export function fetchApi(url: string, options?: RequestInit) {
    // (1) body가 있다면 → JSON 전송을 위한 Content-Type 헤더 자동 추가
    if (options?.body) {
      const headers = new Headers(options.headers || {});
      headers.set("Content-Type", "application/json");
      options.headers = headers;
    }
  
    // (2) 실제 fetch 호출
    return fetch(`${process.env.NEXT_PUBLIC_API_BASE_URL}${url}`, options).then(
      async (res) => {
        // (3) 응답이 실패 상태라면 (status 200~299가 아님)
        if (!res.ok) {
          const rsData = await res.json();
          // 서버가 에러 메시지를 내려줬다면 그걸 throw
          throw new Error(rsData.msg || "요청 실패");
        }
        // (4) 정상 응답이면 JSON으로 반환
        return res.json();
      }
    );
  }
  