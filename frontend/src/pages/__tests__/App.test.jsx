import { cleanup, render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { afterEach, describe, expect, it, vi } from "vitest";
import App from "../../App";
import { clearCurrentUser } from "../../auth";

function mockJsonResponse(data) {
  return Promise.resolve({
    ok: true,
    json: async () => data,
  });
}

describe("App auth flow", () => {
  afterEach(() => {
    cleanup();
    clearCurrentUser();
    vi.restoreAllMocks();
  });

  it("logs in with backend session before entering main route", async () => {
    const user = userEvent.setup();
    global.fetch = vi.fn().mockImplementationOnce(() =>
      mockJsonResponse({
        code: 0,
        message: "success",
        data: {
          token: "demo-token",
          user: {
            userId: 1001,
            userName: "张药师",
            role: "PHARMACIST",
          },
        },
      })
    );

    render(<App />);

    const form = document.querySelector(".auth-form");

    expect(screen.getByRole("heading", { name: "药物管理系统登录" })).toBeInTheDocument();
    expect(screen.getByText("用户号需以 1 或 2 开头")).toBeInTheDocument();

    await user.type(screen.getByLabelText("用户号"), "1001");
    expect(screen.getByText("当前识别角色：药师")).toBeInTheDocument();
    await user.type(screen.getByLabelText("密码"), "pharm123");
    await user.click(form.querySelector('button[type="submit"]'));

    await waitFor(() => {
      expect(screen.getByRole("heading", { name: "主路由" })).toBeInTheDocument();
    });
    expect(screen.getByText(/用户号 1001/)).toBeInTheDocument();
    expect(screen.getByRole("button", { name: "进入药物管理" })).toBeInTheDocument();
  });

  it("registers through backend and switches back to login mode", async () => {
    const user = userEvent.setup();
    global.fetch = vi.fn().mockImplementationOnce(() =>
      mockJsonResponse({
        code: 0,
        message: "success",
        data: {
          userId: 1008,
          userName: "李药师",
          role: "PHARMACIST",
        },
      })
    );

    render(<App />);

    const form = document.querySelector(".auth-form");

    await user.click(screen.getByRole("button", { name: "注册" }));
    await user.type(screen.getByLabelText("用户号"), "1008");
    await user.type(screen.getByLabelText("用户名"), "李药师");
    await user.type(screen.getByLabelText("密码"), "abc123");
    await user.click(form.querySelector('button[type="submit"]'));

    expect(await screen.findByText("注册成功，账号角色为药师，请登录。")).toBeInTheDocument();
    expect(screen.queryByRole("button", { name: "登录中..." })).not.toBeInTheDocument();
    expect(screen.getAllByRole("button", { name: "登录" })).toHaveLength(2);
  });
});
