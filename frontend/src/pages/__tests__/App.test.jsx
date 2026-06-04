import { cleanup, render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { afterEach, beforeEach, describe, expect, it } from "vitest";
import App from "../../App";
import { clearCurrentUser } from "../../auth";

describe("App", () => {
  beforeEach(() => {
    clearCurrentUser();
    global.fetch = () =>
      Promise.resolve({
        ok: true,
        json: async () => ({
          code: 0,
          message: "success",
          data: {
            records: [],
            total: 0,
            pageNum: 1,
            pageSize: 10,
          },
        }),
      });
  });

  afterEach(() => {
    cleanup();
  });

  it("renders login entry before selecting user", () => {
    render(<App />);
    expect(screen.getByRole("heading", { name: "药物管理系统联调入口" })).toBeInTheDocument();
  });

  it("switches to home page after selecting user", async () => {
    const user = userEvent.setup();
    render(<App />);

    await user.click(screen.getByRole("button", { name: /王医生/ }));

    expect(screen.getByRole("heading", { name: "药物管理系统主功能联调准备已就绪" })).toBeInTheDocument();
    expect(screen.getByText("王医生 / DOCTOR")).toBeInTheDocument();
    expect(screen.getByRole("navigation", { name: "主导航" })).toBeInTheDocument();
    expect(screen.getByRole("link", { name: /药品管理/ })).toBeInTheDocument();
    expect(screen.getByRole("link", { name: /库存管理/ })).toBeInTheDocument();
    expect(screen.getByRole("link", { name: /预警查询/ })).toBeInTheDocument();
    expect(screen.getByRole("link", { name: /处方工作台/ })).toBeInTheDocument();
    expect(screen.getByRole("link", { name: /新建处方/ })).toBeInTheDocument();
  });
});
