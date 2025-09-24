import type { NextConfig } from "next";

const nextConfig: NextConfig = {
    async rewrites() {
        return [
            {
                source: "/customer/:path*",
                destination: "http://localhost:8080/customer/:path*",
            },
        ];
    },
};

export default nextConfig;

