import { QueryClient } from "@tanstack/react-query";

/**
 * React Query client singleton used by the app and non-hook modules.
 *
 * @author Hanwon.Jang
 */
export const queryClient = new QueryClient();
