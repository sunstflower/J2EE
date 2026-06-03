import { cleanup, render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { afterEach, beforeEach, describe, expect, it } from "vitest";
import App from "../../App";
import { clearCurrentUser } from "../../auth";

describe("App", () => {
  beforeEach(() => {
    clearCurrentUser();
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
    expect(screen.getByText("当前身份：王医生（DOCTOR）")).toBeInTheDocument();
  });
});
