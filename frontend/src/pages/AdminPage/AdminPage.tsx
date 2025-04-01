import React from 'react';
import Header from '@/components/common/Header/Header';
import { PageContainer } from '@/styles/PageContainer.styles';
import SideBar from '@/pages/AdminPage/components/SideBar/SideBar';
import { Outlet } from 'react-router-dom';
import * as Styled from './AdminPage.styles';
import { useGetClubDetail } from '@/hooks/queries/club/useGetClubDetail';
import { useClubContext } from '@/context/clubContext';

const AdminPage = () => {
  const { clubId } = useClubContext();

  const { data: clubDetail, error } = useGetClubDetail(clubId || '');

  if (!clubDetail) {
    return null;
  }

  if (error) return <p>Error: {error.message}</p>;

  return (
    <>
      <Header />
      <PageContainer>
        <Styled.AdminPageContainer>
          <SideBar clubName={clubDetail?.name || ''} />
          <Styled.Content>
            <Outlet context={clubDetail} />
          </Styled.Content>
        </Styled.AdminPageContainer>
      </PageContainer>
    </>
  );
};

export default AdminPage;
